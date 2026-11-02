#!/bin/sh
set -eu

port="${PORT:-8080}"
sed -i "s/port=\"8080\"/port=\"${port}\"/" "$CATALINA_HOME/conf/server.xml"
exec "$CATALINA_HOME/bin/catalina.sh" run
