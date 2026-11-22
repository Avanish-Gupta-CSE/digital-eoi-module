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

# Prints: <fieldId> <optionIdForStatus>
status_ids() {
  gh project field-list "$NUMBER" --owner "$OWNER" --format json |
    python3 -c "
import json,sys
f=next(f for f in json.load(sys.stdin)['fields'] if f['name']=='$FIELD')
opt=[o['id'] for o in f['options'] if o['name']=='$1']
if not opt:
    names='|'.join(o['name'] for o in f['options'])
    print('unknown status \'$1\'. available: '+names); sys.exit(1)
print(f['id'], opt[0])
"
}

item_for_issue() {
  gh project item-list "$NUMBER" --owner "$OWNER" --format json --limit 200 |
    python3 -c "import json,sys;items=json.load(sys.stdin)['items']
m=[i['id'] for i in items if (i.get('content') or {}).get('number')==int('$1')]
print(m[0] if m else '')"
}

apply_status() {
  item="$1"; status="$2"; issue="$3"
  ids=$(status_ids "$status") || { echo "$ids" >&2; exit 1; }
  field_id=${ids% *}
  option_id=${ids#* }
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
    title="$2"; body="$3"; status="${4:-Todo}"
    url=$(gh issue create --repo "$REPO" --title "$title" --body "$body")
    number=$(echo "$url" | grep -o '[0-9]*$')
    # item-add returns the new item id, so no eventual-consistency lookup is needed.
    item=$(gh project item-add "$NUMBER" --owner "$OWNER" --url "$url" --format json --jq .id)
    echo "created #$number: $title"
    apply_status "$item" "$status" "$number"
    ;;
  move)
    [ -n "$3" ] || { echo "usage: board.sh move <issue> <Status>" >&2; exit 1; }
    item=$(item_for_issue "$2")
    [ -n "$item" ] || { echo "issue #$2 is not on the board" >&2; exit 1; }
    apply_status "$item" "$3" "$2"
    ;;
  *)
    sed -n '2,9p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
    ;;
esac
