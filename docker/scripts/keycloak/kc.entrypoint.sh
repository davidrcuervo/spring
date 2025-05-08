#!/bin/bash

if [ -z "$KC_BOOTSTRAP_ADMIN_USERNAME" ]; then
  echo "Variable, KC_BOOTSTRAP_ADMIN_USERNAME, is unset" >&2
  exit 1
fi

if [ -z "$KC_BOOTSTRAP_ADMIN_ENC_PASSWORD" ]; then
  echo "Variable, KC_BOOTSTRAP_ADMIN_ENC_PASSWORD, is unset" >&2
  exit 1
fi

if [ -z "$SCHEMA_TYPE" ]; then
  echo "Variable, SCHEMA_TYPE, is unset" >&2
  exit 1
fi

if [ -z "$SCHEMA_KEYCLOAK_URL" ]; then
  echo "Variable, SCHEMA_KEYCLOAK_URL, is unset" >&2
  exit 1
fi

if [ -z "$SCHEMA_KEYCLOAK_USERNAME" ]; then
  echo "Variable, SCHEMA_KEYCLOAK_USERNAME, is unset" >&2
  exit 1
fi

if [ -z "$SCHEMA_KEYCLOAK_ENC_PASSWORD" ]; then
  echo "Variable, SCHEMA_KEYCLOAK_ENC_PASSWORD, is unset" >&2
  exit 1
fi

export KC_BOOTSTRAP_ADMIN_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$KC_BOOTSTRAP_ADMIN_ENC_PASSWORD")

SCHEMA_KEYCLOAK_ENC_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$SCHEMA_KEYCLOAK_ENC_PASSWORD")

$HOME/keycloak-26.1.5/bin/kc.sh start-dev \
--db=$SCHEMA_TYPE \
--db-url=$SCHEMA_KEYCLOAK_URL \
--db-username= \
--http-port=$PORT_KEYCLOAK \
--db-password=$SCHEMA_KEYCLOAK_ENC_PASSWORD