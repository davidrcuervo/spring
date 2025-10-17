#!/bin/bash

DIR="$HOME/src"
OUTPUT_DIR="$HOME/target"

echo "Compiling starting..."

function compile() {
  mvn clean -Pcontainer -Dcontainer.target.dir=$OUTPUT_DIR -f $DIR/pom.xml

  if mvn install -DskipTests \
  -Pcontainer -Dcontainer.target.dir=$OUTPUT_DIR \
  -f $DIR/pom.xml; then
    echo "INFO: Compiling finished successfully!!!"
  else
    echo "ERROR: Compiling failed. Sorry."
    exit 1
  fi
}

function copy_dependencies() {
    if mvn dependency:copy-dependencies -Pcontainer \
    -DoutputDirectory=$HOME/lib \
    -f $DIR/pom.xml; then
      echo "INFO: Dependencies copied successfully!!!"
    else
      echo "ERROR: Dependencies failed to copy. Sorry."
    fi
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
copy_dependencies