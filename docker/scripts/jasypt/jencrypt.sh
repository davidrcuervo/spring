#!/bin/bash

SECRET_FILE="/run/secrets/jasypt-password"
DIR=$(dirname "$0")

function encrypt(){
  $DIR/jasypt-1.9.3/bin/encrypt.sh \
  password="$2" \
  input="$1" \
  algorithm="PBEWITHHMACSHA512ANDAES_256" \
  saltGeneratorClassName="org.jasypt.salt.RandomSaltGenerator" \
  ivGeneratorClassName="org.jasypt.iv.RandomIvGenerator" \
  stringOutputType="base64" verbose=false
}

if [ -z "$1" ]; then
  echo "Encrypted word has not been provided." >&2
  exit 1
fi

VALUE="$1"

if [ ! -z "$2" ]; then
  encrypt $VALUE $2

elif [ -f  "$SECRET_FILE" ]; then
  SECRET=$(cat "$SECRET_FILE")
  encrypt $VALUE $SECRET

else
  echo "secret decryptor word missing." >&2
  exit 1
fi

exit 0