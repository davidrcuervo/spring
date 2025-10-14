#!/bin/bash

DIR="$(pwd)/src"
OUTPUT_DIR="$(pwd)/target"

function safestart() {
  if "$HOME/bin/test.sh" "$1"; then
    echo "INFO: Test completed successfully."
    $HOME/bin/start.sh "$JAR_FILE" "$2"
  else
    echo "ERROR: Test failed."
    exit 1
  fi
}

if [ -z "$1" ]; then
  echo "ERROR: Missing package name to compile"
  exit 1
fi

DIR="$DIR/$1"
OUTPUT_DIR="$OUTPUT_DIR/$1"
JAR_FILE="$HOME/target/$1/$1.jar"

echo "INFO: Application folder to compile: $DIR"
echo "INFO: Destination folder: $OUTPUT_DIR"
echo "INFO: Jar File: $JAR_FILE"

safestart "$1" "$2"