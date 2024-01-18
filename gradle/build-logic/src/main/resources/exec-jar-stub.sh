#!/usr/bin/env bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$(cd -P "$(dirname "$SOURCE")" >/dev/null && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$(cd -P "$(dirname "$SOURCE")" >/dev/null && pwd)"

EXEC=$(basename "$0")
SELF="$DIR/$EXEC"

# Make sure TMPDIR is set
TMPDIR=${TMPDIR:-"$(dirname "$(mktemp -u)")"}

# Exec the java process
exec java "$JAVA_OPTS" -jar "$SELF" "$@"
exit 1
