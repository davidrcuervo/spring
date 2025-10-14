#!/bin/bash

DIR="$(pwd)/src"
OUTPUT_DIR="$(pwd)/target"

echo "Compiling starting..."

function compile() {
  mvn clean -Pcontainer -Dcontainer.target.dir=$OUTPUT_DIR -f $DIR/pom.xml

  mvn install -DskipTests \
  -Pcontainer -Dcontainer.target.dir=$OUTPUT_DIR \
  -f $DIR/pom.xml

  echo "...Compiling finished successfully!!!"
}

if [ -z "$1" ]; then
  echo "ERROR: Missing package name to compile"
  exit 1
fi

DIR="$DIR/$1"
OUTPUT_DIR="$OUTPUT_DIR/$1"
echo "INFO: Application folder to compile: $DIR"
echo "INFO: Destination folder: $OUTPUT_DIR"

compile