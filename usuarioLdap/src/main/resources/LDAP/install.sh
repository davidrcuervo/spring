#!/usr/bin/env bash
set -o xtrace

#TEST IF USER IR ROOT
if [ $(id -u) != 0 ];
then
   echo "FAILED: This script requires root permissions"
   #  sudo "$0" "$@"
   exit -1
else
  echo "INFO: Script is running with root permissions"
fi

#CREATE /var/openldap DIRECTORY
if [ -d "/var/openldap" ];
then
  echo "INFO: /var/openldap directory does exist."
else
  mkdir /var/openldap
fi

#CREATE /var/openldap/la-etienda DIRECTORY AND SET OPENLDAP OWNER
if [ -d "/var/openldap/la-etienda.com" ];
then
  echo "FAIL: /var/openldap/la-etienda.com directory does exist."
  exit -1
else
  mkdir /var/openldap/la-etienda.com
fi

chown openldap:openldap /var/openldap/la-etienda.com

#RUN LDIF DIRECTORY FILE TO CREATE OLCDATABASE IN LDAP
ldapadd -Y EXTERNAL -H ldapi:/// -f olcDatabase.ldif
ldapadd -H ldap://127.0.0.1:389 -D "cn=sysadmin,dc=la-etienda,dc=com" -f la-etienda.ldif -W

#INSTALL DATABASES

## CREATE USER DB AND DATABASES
sudo -u postgres bash -c "psql < setup.sql"

##CREATE TABLES IN SPRING SESSION DB
psql -h 127.0.0.1 -p 5432 -U springsession -d springsession -f springsession.table.sql -W

echo "Install script has finished successfully"