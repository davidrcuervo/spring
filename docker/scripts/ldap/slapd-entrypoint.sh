#!/bin/bash

LDIF_FILEPATH=/opt/myslapd/etienda/scripts/etienda.ldif
#LDAP_USER="cn=sysadmin,dc=la-etienda,dc=com"
#LDAP_USERPASSWD=$(cat /run/secrets/ldap-password)

#Start slapd and load etienda directory.
#/etc/init.d/slapd start && \
#ldapmodify -H ldap://127.0.0.1:389 -D $LDAP_USER -w "$LDAP_USERPASSWD" -f $LDIF_FILEPATH && \
#/etc/init.d/slapd stop

/bin/bash -c "slapmodify -n 2 -w < $LDIF_FILEPATH"

#Start slapd to start service in container
/usr/sbin/slapd -u openldap -g openldap -F /etc/ldap/slapd.d -h "ldaps:/// ldapi:/// ldap:///" -d stats
