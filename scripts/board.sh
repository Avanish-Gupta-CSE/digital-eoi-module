#!/bin/sh
# Drive the GitHub Project board with one command, so agents never touch GraphQL ids.
#
#   scripts/board.sh list
#   scripts/board.sh add "Title" "Body" [Status]
#   scripts/board.sh move <issue-number> <Status>
#
# Status is the project's Status field: Todo | In Progress | Done.
set -e

OWNER="@me"
NUMBER=2
REPO="Avanish-Gupta-CSE/digital-eoi-module"
FIELD="Status"

need() { command -v "$1" >/dev/null 2>&1 || { echo "missing $1" >&2; exit 1; }; }
need gh
need python3

project_id() {
  gh project list --owner "$OWNER" --format json |
    python3 -c "import json,sys;print(next(p['id'] for p in json.load(sys.stdin)['projects'] if p['number']==$NUMBER))"
}

status_field() {
  gh project field-list "$NUMBER" --owner "$OWNER" --format json |
    python3 -c "import json,sys;f=next(f for f in json.load(sys.stdin)['fields'] if f['name']=='$FIELD');print(f['id'],','.join(o['id']+':'+o['name'] for o in f['options']))"
}

item_for_issue() {
  gh project item-list "$NUMBER" --owner "$OWNER" --format json --limit 200 |
    python3 -c "import json,sys;items=json.load(sys.stdin)['items']
m=[i['id'] for i in items if (i.get('content') or {}).get('number')==int('$1')]
print(m[0] if m else '')"
}

set_status() {
  issue="$1"; status="$2"
  item=$(item_for_issue "$issue")
  if [ -z "$item" ]; then
    echo "issue #$issue is not on the board" >&2
    exit 1
  fi
  field_line=$(status_field)
  field_id=${field_line%% *}
  option_id=$(echo "${field_line#* }" | tr ',' '\n' | awk -F: -v want="$status" '$2==want{print $1}')
  if [ -z "$option_id" ]; then
    echo "unknown status '$status'. available: $(echo "${field_line#* }" | tr ',' '\n' | cut -d: -f2 | paste -sd'|' -)" >&2
    exit 1
  fi
  gh project item-edit --project-id "$(project_id)" --id "$item" \
    --field-id "$field_id" --single-select-option-id "$option_id" >/dev/null
  echo "#$issue -> $status"
}

case "$1" in
  list)
    gh project item-list "$NUMBER" --owner "$OWNER" --format json --limit 200 |
      python3 -c "
import json,sys
items=json.load(sys.stdin)['items']
order=['Todo','In Progress','Done']
buckets={s:[] for s in order}
buckets['(no status)']=[]
for i in items:
    c=i.get('content') or {}
    s=i.get('status') or '(no status)'
    buckets.setdefault(s,[]).append((c.get('number'),c.get('title','')))
for s in order+[k for k in buckets if k not in order]:
    rows=buckets.get(s,[])
    print(f'{s} ({len(rows)})')
    for n,t in rows: print(f'   #{n} {t[:60]}')
"
    ;;
  add)
    title="$1"; body="$2"; status="${3:-Todo}"
    url=$(gh issue create --repo "$REPO" --title "$title" --body "$body")
    number=$(echo "$url" | grep -o '[0-9]*$')
    gh project item-add "$NUMBER" --owner "$OWNER" --url "$url" >/dev/null
    echo "created #$number: $title"
    set_status "$number" "$status"
    ;;
  move)
    [ -n "$3" ] || { echo "usage: board.sh move <issue> <Status>" >&2; exit 1; }
    set_status "$2" "$3"
    ;;
  *)
    sed -n '2,9p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
    ;;
esac
