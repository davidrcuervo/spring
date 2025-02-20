#!/bin/sh

DATABASE_WEB_PASSWORD=$(decrypt.sh input="$(cat /run/secrets/db-web-password)" password=$(cat /run/secrets/jasypt-password) verbose=false)
DATABASE_SPRINGSESSION_PASSWORD=$(decrypt.sh input="$(cat /run/secrets/db-springsession-password)" password=$(cat /run/secrets/jasypt-password) verbose=false)
DATABASE_KEYCLOAK_PASSWORD=$(decrypt.sh input=$(cat /run/secrets/db-keycloak-password) password=$(cat /run/secrets/jasypt-password) verbose=false)
DATABASE_SPRINGSESSION_CREATE_DB=/opt/mypostgres/scripts/createSpringsessionDb.sql
DATABASE_SPRINGSESSION_CREATE_TABLES=/opt/mypostgres/scripts/createSpringsessionTables.sql
DATABASE_KEYCLOAK_CREATE_DB=/opt/mypostgres/scripts/createKeycloadkDb.sql
LOG_PATH=/opt/mypostgres/logs/postgresql.$(date +%F.%Hh%Mm%Ss).log
DATA_PATH=/opt/mypostgres/data
HBA_CONF_PATH=/opt/mypostgres/scripts/pg_hba.conf

# Before PostgreSQL can function correctly, the database cluster must be initialized:
/usr/lib/postgresql/16/bin/initdb -D $DATA_PATH

# internal start of server in order to allow set-up using psql-client
# does not listen on external TCP/IP and waits until start finishes
/usr/lib/postgresql/16/bin/pg_ctl -D $DATA_PATH -l $LOG_PATH -o "-c listen_addresses='*'" -o "-c hba_file='$HBA_CONF_PATH'" -w start

# create a user or role
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "CREATE USER webapp WITH PASSWORD '$DATABASE_WEB_PASSWORD';"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "CREATE DATABASE webapp TEMPLATE template0 ENCODING 'UNICODE';"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "ALTER DATABASE webapp OWNER TO webapp;"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "GRANT ALL PRIVILEGES ON DATABASE webapp TO webapp;"

#set springsession database
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "CREATE USER springsession WITH PASSWORD '$DATABASE_SPRINGSESSION_PASSWORD';"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -f $DATABASE_SPRINGSESSION_CREATE_DB
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -d springsession -U springsession -f $DATABASE_SPRINGSESSION_CREATE_TABLES

#set keycloak database
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "CREATE USER keycloak WITH PASSWORD '$DATABASE_KEYCLOAK_PASSWORD';"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -f $DATABASE_KEYCLOAK_CREATE_DB

# stop internal postgres server
trap "./scripts/postgres-exitpoint.sh $DATA_PATH" SIGINT SIGTERM
tail -f $LOG_PATH

exec "$@"