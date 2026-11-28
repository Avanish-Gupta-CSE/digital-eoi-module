#!/bin/sh
# Start the Digital EoI module against the local Oracle Docker database.
#
#   scripts/dev-start.sh            start (creates the schema if it is missing)
#   scripts/dev-start.sh --reset    drop everything first, then start
#
# The script is safe to run again: it reuses the schema and the demo rows if they
# are already there, and rebuilds only what is missing.
#
# Oracle runs inside the Docker container "uco-oracle-free". sqlplus is NOT
# installed on the host, so every SQL file is piped into the container with
# "docker exec -i".
set -e

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
RUN_DIR="$ROOT_DIR/.run"
LOG_FILE="$RUN_DIR/eoi.log"

# The Docker container that holds the Oracle database.
CONTAINER=${EOI_ORACLE_CONTAINER:-uco-oracle-free}
# The Oracle system account, used only to create the application user.
SYSTEM_USER=${EOI_SYSTEM_USER:-system}
SYSTEM_PASSWORD=${EOI_SYSTEM_PASSWORD:-uco_realty_pwd}
# The application account the Spring Boot app connects with.
APP_USER=${EOI_DB_USER:-uco_eoi}
APP_PASSWORD=${EOI_DB_PASSWORD:-uco_eoi_pwd}
# The pluggable database and the listener port.
SERVICE=${EOI_DB_SERVICE:-FREEPDB1}
PORT=${EOI_DB_PORT:-1521}
# The HTTP port of the application.
APP_PORT=${SERVER_PORT:-8082}

# Where sqlplus lives inside the Oracle Free image.
SQLPLUS=/opt/oracle/product/23ai/dbhomeFree/bin/sqlplus

RESET=0
for arg in "$@"; do
    case "$arg" in
        --reset) RESET=1 ;;
        -h|--help)
            sed -n '2,12p' "$0" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        *)
            echo "unknown option: $arg (try --help)" >&2
            exit 1
            ;;
    esac
done

mkdir -p "$RUN_DIR"

# Runs a SQL script as the system user. The script comes from stdin, so the
# same call works for one-liners and for the migration files.
run_as_system() {
    printf '%s\n' "$1" |
        docker exec -i "$CONTAINER" "$SQLPLUS" -s \
            "$SYSTEM_USER/$SYSTEM_PASSWORD@//localhost:$PORT/$SERVICE"
}

# Runs a SQL file (or stdin when no file argument is passed) as the application user.
run_app_file() {
    if [ -n "$1" ]; then
        docker exec -i "$CONTAINER" "$SQLPLUS" -s \
            "$APP_USER/$APP_PASSWORD@//localhost:$PORT/$SERVICE" < "$1"
    else
        docker exec -i "$CONTAINER" "$SQLPLUS" -s \
            "$APP_USER/$APP_PASSWORD@//localhost:$PORT/$SERVICE"
    fi
}

echo "==> Checking the Oracle container"
if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
    echo "the container '$CONTAINER' is not running." >&2
    echo "start it with: docker start $CONTAINER" >&2
    exit 1
fi

echo "==> Waiting for Oracle to accept connections"
attempt=0
until run_as_system "SELECT 1 FROM DUAL; EXIT;" >/dev/null 2>&1; do
    attempt=$((attempt + 1))
    if [ "$attempt" -gt 30 ]; then
        echo "Oracle did not answer after 30 tries." >&2
        exit 1
    fi
    sleep 2
done

echo "==> Creating the '$APP_USER' account if it is missing"
run_as_system "
SET PAGESIZE 0
SET LINESIZE 200
SET FEEDBACK OFF
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM all_users WHERE username = '$APP_USER';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE USER $APP_USER IDENTIFIED BY $APP_PASSWORD';
        EXECUTE IMMEDIATE 'GRANT CONNECT, RESOURCE, CREATE TABLE, CREATE SEQUENCE, UNLIMITED TABLESPACE TO $APP_USER';
    END IF;
END;
/
COMMIT;
EXIT;
" >/dev/null

# Grant read access to HRMS_DBA tables (must run as hrms_dba, since system lacks GRANT OPTION on them).
printf "GRANT SELECT ON hrms_dba.employee_contact_details TO $APP_USER;\nGRANT SELECT ON hrms_dba.employee_tenure TO $APP_USER;\nEXIT;\n" |
    docker exec -i "$CONTAINER" "$SQLPLUS" -s \
        "hrms_dba/hrms_dba_pwd@//localhost:$PORT/$SERVICE" >/dev/null 2>&1 || true

if [ "$RESET" -eq 1 ]; then
    echo "==> --reset: dropping the EoI tables and the sequence"
    # Children first, so the foreign keys do not block the drop.
    run_app_file <<'SQL' >/dev/null
SET FEEDBACK OFF
DROP TABLE eoi_audit_log;
DROP TABLE eoi_report_rubric;
DROP TABLE eoi_report_experience;
DROP TABLE eoi_currency_position;
DROP TABLE eoi_derivative;
DROP TABLE eoi_facility_security;
DROP TABLE eoi_facility;
DROP TABLE eoi_borrower_bank;
DROP TABLE eoi_borrower_allied;
DROP TABLE eoi_borrower_director;
DROP TABLE eoi_report;
DROP TABLE eoi_borrower;
DROP SEQUENCE eoi_seq;
EXIT;
SQL
fi

echo "==> Checking whether the schema is already there"
TABLE_COUNT=$(run_as_system "
SET PAGESIZE 0
SET FEEDBACK OFF
SELECT COUNT(*) FROM all_tables WHERE owner = '$APP_USER' AND table_name = 'EOI_REPORT';
EXIT;
" | tr -d '[:space:]')

if [ "$TABLE_COUNT" = "0" ]; then
    echo "==> Creating the schema (01_CREATE_EOI_TABLES.sql)"
    run_app_file "$ROOT_DIR/db_migrations/01_CREATE_EOI_TABLES.sql" >/dev/null
    echo "==> Loading the demo rows (02_SEED_EOI_DEMO.sql)"
    run_app_file "$ROOT_DIR/db_migrations/02_SEED_EOI_DEMO.sql" >/dev/null
else
    echo "==> The schema is already there, skipping the migrations"
fi

echo "==> Building the application"
cd "$ROOT_DIR"
mvn -o -q clean package -DskipTests

echo "==> Starting the application on port $APP_PORT"
DEV_LOGIN_ENABLED=true EOI_DB_USER="$APP_USER" EOI_DB_PASSWORD="$APP_PASSWORD" nohup mvn -o spring-boot:run -Dspring-boot.run.arguments="--server.port=$APP_PORT" > "$LOG_FILE" 2>&1 < /dev/null &
APP_PID=$!
echo "$APP_PID" > "$RUN_DIR/eoi.pid"

echo "==> Waiting for the application to start"
attempt=0
until grep -q "Started DigitalEoiApplication" "$LOG_FILE" 2>/dev/null; do
    attempt=$((attempt + 1))
    if [ "$attempt" -gt 90 ]; then
        echo "the application did not start. Last lines of $LOG_FILE:" >&2
        tail -30 "$LOG_FILE" >&2
        exit 1
    fi
    sleep 2
done

echo "==> Checking http://localhost:$APP_PORT/login and /dashboard"
HTTP_CODE=$(curl -s -L -o /dev/null -w '%{http_code}' "http://localhost:$APP_PORT/dashboard")
if [ "$HTTP_CODE" = "200" ]; then
    echo "    200 OK - login & dashboard guard answer against Oracle"
else
    echo "    answered with HTTP $HTTP_CODE" >&2
    tail -30 "$LOG_FILE" >&2
    exit 1
fi

echo "==> Stopping the application again, so port $APP_PORT is free"
pkill -P "$APP_PID" 2>/dev/null || true
kill "$APP_PID" 2>/dev/null || true
wait "$APP_PID" 2>/dev/null || true
rm -f "$RUN_DIR/eoi.pid"

echo
echo "Ready. Start it yourself with:"
echo "  mvn -o spring-boot:run -Dspring-boot.run.arguments=\"--server.port=$APP_PORT\""
echo "The log of the last run is in $LOG_FILE"
