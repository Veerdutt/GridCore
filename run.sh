#!/usr/bin/env bash
set -e
mkdir -p out
javac -d out src/*.java src/warehouse/*.java src/pathfinding/*.java src/routing/*.java src/dispatch/*.java
java -cp out Main "$@"
