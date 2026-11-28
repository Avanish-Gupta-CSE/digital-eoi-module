#!/bin/sh
# Guardrails that must hold on every push. Plain grep, no dependencies. Exit 1 blocks the PR.
# Rules are explained in .opencode/guardrails/security-and-integrity.md (numbers in brackets).
# A check that fails prints the file and line, so the fix is obvious.
set -e
fail=0
JSP_DIR=src/main/webapp

report() {
  echo "BLOCKER $1"
  echo "$2"
  echo "  -> $3"
  fail=1
}

if [ -d src ]; then
  # Bank CSP: no inline script and no inline style inside a JSP.
  # An external <script src="..."> is fine; a <script> without src is inline.
  hits=$(grep -rn --include='*.jsp' --include='*.jspf' -E '<script|style="' src 2>/dev/null \
    | grep -vE '<script[^>]* src=' || true)
  [ -n "$hits" ] && report "inline <script> or style=\"\" in a JSP:" "$hits" \
    "move handlers to assets/js/ and styles to assets/css/style.css"

  # [no logic in JSP] scriptlets and expressions.
  hits=$(grep -rn --include='*.jsp' --include='*.jspf' -E '<%[^-@!]' $JSP_DIR 2>/dev/null || true)
  [ -n "$hits" ] && report "scriptlet in a JSP:" "$hits" "move the logic to a controller or service"

  # [1] printed values must go through <c:out>. Lines that only build URLs, tags or attributes of
  # c: tags, or that are JSP comments, are fine.
  hits=$(grep -rnE --include='*.jsp' --include='*.jspf' '\$\{[^}]*\}' $JSP_DIR 2>/dev/null \
    | grep -vE 'c:out|escapeXml|contextPath|<c:(if|when|forEach|set|choose|url|param)|href=|action=|src=|_csrf|<%--|path=|items=|test=' || true)
  [ -n "$hits" ] && report "[rule 1] unescaped \${...} printed in a JSP:" "$hits" \
    "wrap it: <c:out value=\"\${...}\"/> (also inside attributes)"

  # [2] every <form> needs the _csrf hidden field in the same file.
  for f in $(grep -rlE --include='*.jsp' --include='*.jspf' '<form(:form)?[ >]' $JSP_DIR 2>/dev/null); do
    forms=$(grep -cE '<form(:form)?[ >]' "$f")
    tokens=$(grep -c 'name="_csrf"' "$f" || true)
    if [ "$forms" -gt "$tokens" ]; then
      report "[rule 2] form without a CSRF token in $f ($forms forms, $tokens tokens)" "" \
        "add <input type=\"hidden\" name=\"_csrf\" value=\"<c:out value='\${sessionScope._csrf}'/>\"> inside each form"
    fi
  done

  # SQL must use bound parameters: a SQL literal followed by "+ variable" is concatenation.
  # The configured HRMS schema name is the one allowed exception.
  hits=$(grep -rnE --include='*.java' '"[^"]*(SELECT|UPDATE|DELETE|INSERT|WHERE|FROM)[^"]*" *\+ *[a-zA-Z]' src/main \
    | grep -v 'schema' || true)
  [ -n "$hits" ] && report "SQL built by string concatenation:" "$hits" "use a bound ? parameter"

  # [9] no hard-coded credential in Java or properties (values from the environment).
  hits=$(grep -rniE --include='*.java' '(password|passwd|pwd|secret|apikey)[A-Za-z_]*[ ]*=[ ]*"[^"$]{4,}"' src/main || true)
  hits="$hits$(grep -rniE --include='*.properties' '^[^#]*(password|secret)[^=]*=[A-Za-z0-9_]{4,}$' src/main || true)"
  [ -n "$hits" ] && report "[rule 9] hard-coded credential:" "$hits" "read it from an environment variable"
fi

if [ "$fail" -ne 0 ]; then
  exit 1
fi

echo "guardrails: ok"
