#!/bin/bash
JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password)
DIR=$(pwd)

function run() {
    mvn spring-boot:run \
    "-Dspring-boot.run.jvmArguments=-Djasypt.encryptor.password=$JASYPT_PASSWORD \
    -Dspring.config.additional-location=$DIR/spring/API/,$DIR/spring/" \
    -f "$1"
}

function start() {
  java -jar "$1" \
  -Djasypt.encryptor.password=$JASYPT_PASSWORD \
  -Dspring.config.additional-location=$DIR/spring/API/,$DIR/spring/
}

if [ -f "$1" ]; then
  start $1
else
  echo "Failed to start. pom file is not valid" >&2
fi

