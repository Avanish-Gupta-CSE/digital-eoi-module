#!/bin/sh
# Guardrails that must hold on every push. Deliberately boring: two greps, no cleverness.
# Exit code 1 blocks the PR.
set -e
fail=0

if [ -d src ]; then
  # Bank CSP: no inline script and no inline style inside a JSP.
  hits=$(grep -rn --include='*.jsp' -E '<script|style="' src 2>/dev/null || true)
  if [ -n "$hits" ]; then
    echo "BLOCKER: inline <script> or style=\"\" found in a JSP:"
    echo "$hits"
    echo "Move handlers to assets/js/ and styles to assets/css/style.css."
    fail=1
  fi
fi

if [ "$fail" -ne 0 ]; then
  exit 1
fi

echo "guardrails: ok"
