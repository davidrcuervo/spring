#!/bin/bash

KC_ADMIN_PASSWORD=$(decrypt.sh input="$(cat /run/secrets/kc-admin-password)" password=$(cat /run/secrets/jasypt-password) verbose=false)
KC_FRONTEND_CLIENT_ID_SECRET=$(decrypt.sh input="$(cat /run/secrets/kc-frontend-clientId-secret)" password=$(cat /run/secrets/jasypt-password) verbose=false)
KC_USER_CLIENT_ID_SECRET=$(decrypt.sh input="$(cat /run/secrets/kc-user-clientId-secret)" password=$(cat /run/secrets/jasypt-password) verbose=false)
WEBAPP_ADMIN_PASSWORD=$(decrypt.sh input="$(cat /run/secrets/webapp-admin-password)" password=$(cat /run/secrets/jasypt-password) verbose=false)
EMAIL_SMTP_PASSWORD=$(decrypt.sh input="$(cat /run/secrets/webapp-admin-password)" password=$(cat /run/secrets/jasypt-password) verbose=false)

#set credentials
kcadm.sh config credentials \
--server http://keycloaket:8001 \
--realm master \
--user etadmuser \
--password $KC_ADMIN_PASSWORD

#SET EMAIL TO etadmuser
##Get user id
HOME_MASTER_USER_ID=$(kcadm.sh get users -r master \
-q q=username:etadmuser \
--fields id \
--format csv --noquotes)
##Set email
kcadm.sh update users/$HOME_MASTER_USER_ID -r master \
-s firstName="Keycloak" \
-s lastName="Admin User" \
-s email=myself@la-etienda.com \
-s emailVerified=true

#create realm
REALM_ID=$(kcadm.sh create realms -s realm=etrealm \
-s enabled=true \
-s verifyEmail=true \
-s loginWithEmailAllowed=true \
-s registrationAllowed=true \
-s resetPasswordAllowed=true \
-s rememberMe=true -i)
kcadm.sh update realms/$REALM_ID -f - << EOF
{"smtpServer" : {
    "starttls" : "true",
    "auth" : "true",
    "ssl" : "true",
    "password" : "$EMAIL_SMTP_PASSWORD",
    "port" : "465",
    "host" : "smtp.zoho.com",
    "from" : "myself@la-etienda.com",
    "fromDisplayName" : "La e-Tienda",
    "user" : "myself@la-etienda.com"
  }
}
EOF

#CREATE KC CLIENTS (one for each microservice)
#create client for frontend
kcadm.sh create clients -r etrealm -f - << EOF
{
  "clientId":"et-frontend-kc-client",
  "name":"Et. Frontend KC Client",
  "enabled":"true",
  "clientAuthenticatorType":"client-secret",
  "secret":"$KC_FRONTEND_CLIENT_ID_SECRET",
  "redirectUris":["http://127.0.0.1:8080/*"]
}
EOF

#create client for user microservice
kcadm.sh create clients -r etrealm -f - << EOF
{
  "clientId":"et-user-kc-client",
  "name":"Et. User KC Client",
  "enabled":"true",
  "clientAuthenticatorType":"client-secret",
  "secret":"$KC_USER_CLIENT_ID_SECRET",
  "redirectUris":["http://127.0.0.1:8080/*"],
  "directAccessGrantsEnabled":"true"
}
EOF

#create realm-roles
kcadm.sh create roles -r etrealm -s name=role_manager -s 'description=Manager of the application.'

#create users
kcadm.sh create users -r etrealm -s username=etadmuser -s enabled=true \
-s email=admin@la-etienda.com \
-s firstName="Keycloak" \
-s lastName="Realm Admin" \
-s emailVerified=true
kcadm.sh add-roles --uusername etadmuser --rolename role_manager -r etrealm
kcadm.sh set-password -r etrealm --username etadmuser --new-password $WEBAPP_ADMIN_PASSWORD

#enable keycloak to send roles in token (userinfo.token.claim: true)
ROLES_SCOPE_ID=$(kcadm.sh get client-scopes -r etrealm -F id,name --format csv --noquotes | grep roles | awk -F "," '{print $1}')
REALM_ROLE_MAPPER_ID=$(kcadm.sh get client-scopes/$ROLES_SCOPE_ID/protocol-mappers/models -r etrealm -F id,name --format csv --noquotes | grep "realm roles" | awk -F "," '{print $1}')
kcadm.sh update client-scopes/$ROLES_SCOPE_ID/protocol-mappers/models/$REALM_ROLE_MAPPER_ID -r etrealm -f - << EOF
{
  "id" : "$REALM_ROLE_MAPPER_ID",
  "name" : "realm roles",
  "protocol" : "openid-connect",
  "protocolMapper" : "oidc-usermodel-realm-role-mapper",
  "consentRequired" : false,
  "config" : {
    "introspection.token.claim" : "true",
    "multivalued" : "true",
    "userinfo.token.claim" : "true",
    "user.attribute" : "foo",
    "id.token.claim" : "false",
    "lightweight.claim" : "false",
    "access.token.claim" : "true",
    "claim.name" : "realm_access.roles",
    "jsonType.label" : "String"
  }
}{ "config" : {
    "userinfo.token.claim" : "true",
  }
}
EOF