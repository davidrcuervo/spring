#!/bin/bash

SECRET_FILE="/run/secrets/jasypt-password"
#SECRET_FILE="$(dirname $(dirname $(pwd)))/private/jasypt-password.txt"
#POM_XML="$(pwd)/pom.xml"
POM_XML="/opt/jasypt/pom.xml"
DIR=$(dirname "$0")

function decrypt_with_script(){
  $DIR/jasypt-1.9.3/bin/decrypt.sh \
  password="$2" \
  input="$1" \
  algorithm="PBEWITHHMACSHA512ANDAES_256" \
  saltGeneratorClassName="org.jasypt.salt.RandomSaltGenerator" \
  ivGeneratorClassName="org.jasypt.iv.RandomIvGenerator" \
  stringOutputType="base64" verbose=false
}

function_decrypt(){
mvn jasypt:decrypt-value \
-Djasypt.encryptor.password="$2" \
-Djasypt.plugin.value="$1" \
-Dmaven.repo.local="$HOME/.m2" \
-f "$POM_XML" \
| grep -v "Downloading" | grep -v "Downloaded" \
| grep -v "^\[INFO\]" | grep -v "^\[WARNING\]" | grep -v "^\[ERROR\]" | grep -v "^$"
}

if [ -z "$1" ]; then
  echo "Encrypted word has not been provided." >&2
  exit 1
fi

ENCRYPTED_VALUE="$1"
VALUE="${ENCRYPTED_VALUE#ENC(}"
VALUE="${VALUE%)}"

if [ ! -z "$2" ]; then
  function_decrypt $VALUE $2

elif [ -f  "$SECRET_FILE" ]; then
  SECRET=$(cat "$SECRET_FILE")
  decrypt_with_script $VALUE $SECRET

else
  echo "secret decryptor word missing." >&2
  exit 1
fi

exit 0