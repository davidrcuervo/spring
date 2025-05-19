#!/usr/bin/env bash

function run(){
  export DOLLAR="$"

  echo "Setting nginx site configuration file"
  envsubst < keycloak.template.nginx > /etc/nginx/sites-available/keycloak.nginx
  envsubst < webapp.template.nginx > /etc/nginx/sites-available/webapp.nginx
  ln -s /etc/nginx/sites-available/keycloak.nginx /etc/nginx/sites-enabled/keycloak
  ln -s /etc/nginx/sites-available/webapp.nginx /etc/nginx/sites-enabled/webapp

  echo "Starting nginx service"
  nginx -g "daemon off;"
}

if [ ! -f keycloak.template.nginx ];then
  echo "ERROR, file keycloak.template.nginx does not exitst" >&2
  exit 1
fi

if [ ! -f webapp.template.nginx ];then
  echo "ERROR, file webapp.template.nginx does not exitst" >&2
  exit 1
fi

if [ -z "$PORT_KEYCLOAK" ]; then
  echo "ERROR: env variable PORT_KEYCLOAK is unset" >&2
  exit 1
fi

if [ -z "$KC_PUBLIC_ADDRESS" ]; then
  echo "ERROR: env variable KC_PUBLIC_ADDRESS is unset" >&2
  exit 1
fi

if [ -z "$PORT_FRONTEND" ]; then
  echo "ERROR: env variable PORT_FRONTEND is unset" >&2
  exit 1
fi

if [ -z "$WEBAPP_PUBLIC_ADDRESS" ]; then
  echo "ERROR: env variable WEBAPP_PUBLIC_ADDRESS is unset" >&2
  exit 1
fi

run
