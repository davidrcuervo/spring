#!/bin/bash

JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password)

function test_deprecated() {
  mvn surefire:test -DskipCompile -Dcontainer.target.dir=$DIR \
  -Djasypt.encryptor.password=$JASYPT_PASSWORD \
  -Dspring.config.additional-location="$HOME/API/","$HOME/etc/" \
  -f $HOME/bin/pom.test.xml
#  -Djdk.attach.allowAttachSelf=true \
}

function test() {
  echo "INFO: Application folder to test: $DIR"
  java -cp $HOME/lib/junit-platform-console-standalone-6.0.0.jar:$DIR/classes:$DIR/test-classes:$HOME/lib/* \
  -Djasypt.encryptor.password=$JASYPT_PASSWORD \
  -Dspring.config.additional-location="$HOME/API/","$HOME/etc/" \
  org.junit.platform.console.ConsoleLauncher execute \
  --scan-classpath
}

if [ -z "$1" ]; then
  echo "ERROR: Missing package name to test"
  exit 1
fi

DIR="$HOME/target/$1"

test