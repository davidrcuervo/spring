#!/bin/bash
set -e

APP_TEST_USER_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$APP_USER_TEST_ENC_PASSWORD")
APP_USER_ADMIN_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$APP_USER_ADMIN_ENC_PASSWORD")

kcadm.sh config credentials \
--server http://keycloaket:8001 \
--realm etrealm \
--user $APP_USER_ADMIN_USERNAME \
--password $APP_USER_ADMIN_PASSWORD

kcadm.sh config credentials \
--server http://keycloaket:8001 \
--realm etrealm \
--user $APP_USER_TEST_USERNAME \
--password $APP_TEST_USER_PASSWORD