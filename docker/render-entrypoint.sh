#!/bin/sh
set -eu

PORT="${PORT:-8080}"
case "$PORT" in
    ''|*[!0-9]*)
        echo "PORT must be a number. Render sets this automatically." >&2
        exit 1
        ;;
esac

# Change only the HTTP connector. Leave the shutdown port alone.
sed -i "0,/port=\"8080\"/s//port=\"${PORT}\"/" /usr/local/tomcat/conf/server.xml

exec catalina.sh run
