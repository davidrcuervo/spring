#!/usr/bin/env bash

set -e

if [ -z "$APP_USER_TEST_ENC_PASSWORD" ]; then
  exit 1
fi

if [ -z "$APP_USER_TEST_USERNAME" ]; then
  exit 1
fi

APP_TEST_USER_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$APP_USER_TEST_ENC_PASSWORD")

kcadm.sh config credentials \
--server http://keycloaket:8001 \
--realm etrealm \
--user "$APP_USER_TEST_USERNAME" \
--password "$APP_TEST_USER_PASSWORD" > /dev/null 2>&1 || exit 1

exit 0