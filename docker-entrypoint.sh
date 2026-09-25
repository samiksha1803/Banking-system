#!/bin/bash
set -e

PORT="${PORT:-8080}"
CONF=/usr/local/tomcat/conf/server.xml

# Shutdown port must not accept HTTP; Render health checks were hitting it (HEAD / warnings).
sed -i 's/port="8005"/port="-1"/' "$CONF"

# HTTP connector must listen on Render's PORT (not 8080).
if grep -q 'port="8080" protocol="HTTP/1.1"' "$CONF"; then
  sed -i "s|port=\"8080\" protocol=\"HTTP/1.1\"|port=\"${PORT}\" protocol=\"HTTP/1.1\" address=\"0.0.0.0\"|" "$CONF"
else
  sed -i "s|protocol=\"HTTP/1.1\" port=\"8080\"|protocol=\"HTTP/1.1\" port=\"${PORT}\" address=\"0.0.0.0\"|" "$CONF"
fi

exec /usr/local/tomcat/bin/catalina.sh run
