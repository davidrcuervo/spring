#!/bin/bash

DIR="$(pwd)/src"
OUTPUT_DIR="$(pwd)/target"
JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password)

function test() {
  mvn test -U -Pcontainer -Dcontainer.target.dir=$OUTPUT_DIR \
  -Djasypt.encryptor.password=$JASYPT_PASSWORD \
  -Dspring.config.additional-location="$HOME/API/","$HOME/etc/" \
  -Djdk.attach.allowAttachSelf=true \
  -f $DIR/pom.xml
}

if [ -z "$1" ]; then
  echo "ERROR: Missing package name to test"
  exit 1
fi

DIR="$DIR/$1"
OUTPUT_DIR="$OUTPUT_DIR/$1"
echo "INFO: Application folder to test: $DIR"
echo "INFO: Destination folder: $OUTPUT_DIR"

test