#!/bin/sh
set -e

echo "🚀 Starting EmployeeCheckingPlatform..."

# DB uchun ixtiyoriy kutish (WAIT_FOR_DB=db:5432 bo'lsa)
if [ -n "$WAIT_FOR_DB" ]; then
  HOST=$(echo "$WAIT_FOR_DB" | cut -d: -f1)
  PORT=$(echo "$WAIT_FOR_DB" | cut -d: -f2)
  echo "⏳ Waiting for database at $HOST:$PORT..."
  until nc -z "$HOST" "$PORT"; do
    sleep 1
  done
  echo "✅ Database is up!"
fi

# JVM parametrlari bo'lsa JAVA_TOOL_OPTIONS orqali beriladi
echo "➡️  Launching application..."
exec java $JAVA_TOOL_OPTIONS -jar app.jar
