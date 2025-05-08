#!/bin/bash

#SECRET_FILE="/run/secrets/jasypt-password"
SECRET_FILE="$(dirname $(dirname $(pwd)))/private/jasypt-password.txt"
POM_XML="$(pwd)/pom.xml"

function_decrypt(){
mvn jasypt:decrypt-value \
-Djasypt.encryptor.password="$2" \
-Djasypt.plugin.value="$1" \
-f "$POM_XML" \
| grep -v "Downloading" | grep -v "Downloaded" \
| grep -v "^\[INFO\]" | grep -v "^\[WARNING\]" | grep -v "^\[ERROR\]" | grep -v "^$"
}

if [ -z "$1" ]; then
  echo "Encrypted word has not been provided." >&2
  exit 1
fi

if [ ! -z "$2" ]; then
  echo "here1"
  function_decrypt $1 $2

elif [ -f  "$SECRET_FILE" ]; then
  SECRET=$(cat "$SECRET_FILE")
  function_decrypt $1 $SECRET

else
  echo "secret decryptor word missing." >&2
  exit 1
fi

exit 0