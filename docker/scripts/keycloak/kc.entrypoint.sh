#!/bin/bash

export KC_BOOTSTRAP_ADMIN_PASSWORD=$(decrypt.sh input="$(cat /run/secrets/kc-admin-password)" password=$(cat /run/secrets/jasypt-password) verbose=false)

DB_PASSWORD=$(decrypt.sh input="$(cat /run/secrets/db-keycloak-password)" password=$(cat /run/secrets/jasypt-password) verbose=false)

$HOME/keycloak-26.1.5/bin/kc.sh \
--config-file=$HOME/kc.et.conf \
start-dev \
--http-port=$PORT_KEYCLOAK \
--db-password=$DB_PASSWORD