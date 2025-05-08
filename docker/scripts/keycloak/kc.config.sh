#!/bin/bash

$KC_BOOTSTRAP_ADMIN_ENC_PASSWORD
$KC_BOOTSTRAP_ADMIN_USERNAME
$SMTP_EMAIL_ENC_PASSWORD
$SMTP_EMAIL_PORT
$SMTP_EMAIL_HOST
$SMTP_EMAIL_USERNAME
$KC_FRONTEND_CLIENT_ID
$KC_FRONTEND_CLIENT_ENC_PASSWORD
$KC_USER_CLIENT_ID
$KC_USER_CLIENT_ENC_PASSWORD
$APP_USER_ADMIN_USERNAME
$APP_USER_ADMIN_ENC_PASSWORD
$APP_USER_TEST_USERNAME
$APP_USER_TEST_ENC_PASSWORD

#set admin credentials
KC_ADMIN_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$KC_BOOTSTRAP_ADMIN_ENC_PASSWORD")
kcadm.sh config credentials \
--server http://keycloaket:8001 \
--realm master \
--user $KC_BOOTSTRAP_ADMIN_USERNAME \
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
SMTP_EMAIL_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$SMTP_EMAIL_ENC_PASSWORD")
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
    "password" : "$SMTP_EMAIL_PASSWORD",
    "port" : "$SMTP_EMAIL_PORT",
    "host" : "$SMTP_EMAIL_HOST",
    "from" : "$SMTP_EMAIL_USERNAME",
    "fromDisplayName" : "La e-Tienda",
    "user" : "$SMTP_EMAIL_USERNAME"
  }
}
EOF

#CREATE KC CLIENTS (one for each microservice)
#create client for frontend
KC_FRONTEND_CLIENT_ID_SECRET=$(/opt/jasypt/jdecrypt.sh "$KC_FRONTEND_CLIENT_ENC_PASSWORD")
kcadm.sh create clients -r etrealm -f - << EOF
{
  "clientId":"$KC_FRONTEND_CLIENT_ID",
  "name":"Et. Frontend KC Client",
  "enabled":"true",
  "clientAuthenticatorType":"client-secret",
  "secret":"$KC_FRONTEND_CLIENT_ID_SECRET",
  "redirectUris":["http://127.0.0.1:8080/*"]
}
EOF

#create client for user microservice
KC_USER_CLIENT_ID_SECRET=$(/opt/jasypt/jdecrypt.sh "$KC_USER_CLIENT_ENC_PASSWORD")
kcadm.sh create clients -r etrealm -f - << EOF
{
  "clientId":"$KC_USER_CLIENT_ID",
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
APP_USER_ADMIN_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$APP_USER_ADMIN_ENC_PASSWORD")
kcadm.sh create users -r etrealm -s username=$APP_USER_ADMIN_USERNAME -s enabled=true \
-s email=admin@la-etienda.com \
-s firstName="Realm Admin" \
-s lastName="Keycloak" \
-s emailVerified=true
kcadm.sh add-roles --uusername etadmuser --rolename role_manager -r etrealm
kcadm.sh set-password -r etrealm --username $APP_USER_ADMIN_USERNAME --new-password $APP_USER_ADMIN_PASSWORD

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

##CREATE TEST USER
APP_TEST_USER_PASSWORD=$(/opt/jasypt/jdecrypt.sh "$APP_USER_TEST_ENC_PASSWORD")
kcadm.sh create users -r etrealm -s username=$APP_USER_TEST_USERNAME -s enabled=true \
-s email=myself@la-etienda.com \
-s firstName="Test User" \
-s lastName="Keycloak" \
-s emailVerified=true

#set password to test user
kcadm.sh set-password -r etrealm --username $APP_USER_TEST_USERNAME --new-password $APP_TEST_USER_PASSWORD