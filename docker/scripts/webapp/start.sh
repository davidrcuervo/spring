#!/bin/bash
JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password)

function arret(){
  echo "Stopping container process..."
  echo "$1"
  curl -X POST http://127.0.0.1:$1/actuator/shutdown
  echo "God bye!!!"
  exit 0
}

function run() {
    mvn spring-boot:run \
    "-Dspring-boot.run.jvmArguments=-Djasypt.encryptor.password=$JASYPT_PASSWORD \
    -Dspring.config.additional-location=$DIR/spring/API/,$DIR/spring/" \
    -f "$1"
}

function start() {
  java -ea -Djasypt.encryptor.password=$JASYPT_PASSWORD \
  -Dspring.config.additional-location=$HOME/API/,$HOME/etc/ \
  -jar "$JAR_FILE"
}

if [ -z "$1" ]; then
  echo "ERROR. Missing package name to execute." >&2
  exit 1
fi

if [ -z "$2" ]; then
  echo "ERROR. close command missing." >&2
  exit 1
fi

if [ -z "$JASYPT_PASSWORD" ]; then
  echo "ERROR. Jasypt password is unset." >&2
  exit 1
fi

JAR_FILE="$HOME/target/$1/$1.jar"
echo "INFO: Jar file is: $JAR_FILE"
echo "INFO: Service will run on port: $2"
trap 'arret "$2"' SIGTERM SIGINT SIGQUIT SIGHUP
start $1 &
CHILD_PID=$!
wait "$CHILD_PID"
