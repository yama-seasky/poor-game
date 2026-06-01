#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
mkdir -p "$SCRIPT_DIR/bin"
TMPDIR=$(mktemp -d)
cp "$SCRIPT_DIR/src/"*.java "$TMPDIR/"
cd "$TMPDIR"
javac -encoding UTF-8 -d "$SCRIPT_DIR/bin" *.java
STATUS=$?
rm -rf "$TMPDIR"
exit $STATUS
