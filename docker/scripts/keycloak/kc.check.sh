#!/usr/bin/env bash

set -e
#set -x

if [ -z "$APP_USER_TEST_ENC_PASSWORD" ]; then
  exit 1
fi

if [ -z "$APP_USER_TEST_USERNAME" ]; then
  exit 1
fi

if [ -z "$PORT_KEYCLOAK" ]; then
  exit 1
fi

if [ -z "$KC_PUBLIC_ADDRESS" ]; then
  exit 1
fi

if [ -z "$KC_PUBLIC_PORT" ]; then
  exit 1
fi

APP_TEST_USER_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$APP_USER_TEST_ENC_PASSWORD")

kcadm.sh config credentials \
--server https://$KC_PUBLIC_ADDRESS:$KC_PUBLIC_PORT \
--realm etrealm \
--user "$APP_USER_TEST_USERNAME" \
--password "$APP_TEST_USER_PASSWORD" > /dev/null 2>&1 || exit 1

exit 0