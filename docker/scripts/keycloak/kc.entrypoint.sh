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

if [ -z "$PORT_KEYCLOAK_MANAGEMENT" ]; then
  echo "Variable, PORT_KEYCLOAK_MANAGEMENT, is unset" >&2
  exit 1
fi

export KC_BOOTSTRAP_ADMIN_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$KC_BOOTSTRAP_ADMIN_ENC_PASSWORD")

SCHEMA_KEYCLOAK_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$SCHEMA_KEYCLOAK_ENC_PASSWORD")

echo "keycloak username: $KC_BOOTSTRAP_ADMIN_USERNAME"
echo "keycloak database type: $SCHEMA_TYPE"
echo "keycloak database url: $SCHEMA_KEYCLOAK_URL"
echo "keycloak database username: $SCHEMA_KEYCLOAK_USERNAME"

$HOME/keycloak-26.1.5/bin/kc.sh start \
--db=$SCHEMA_TYPE \
--db-url=$SCHEMA_KEYCLOAK_URL \
--db-username=$SCHEMA_KEYCLOAK_USERNAME \
--db-password=$SCHEMA_KEYCLOAK_PASSWORD \
--health-enabled=true \
--metrics-enabled=true \
--http-management-port $PORT_KEYCLOAK_MANAGEMENT \
--hostname-strict false \
--https-certificate-file=/opt/myjava/certs/kc.crt \
--https-certificate-key-file=/opt/myjava/certs/kc.unsecure.key
#--http-enabled true \
#--proxy-headers=forwarded \
#--http-port=$PORT_KEYCLOAK
#--hostname=auth.webapp.com \
#--hostname-backchannel-dynamic true \