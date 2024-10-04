#!/bin/sh

DATABASE_WEB_PASSWORD=$(cat /run/secrets/db-web-password)
DATABASE_SPRINGSESSION_PASSWORD=$(cat /run/secrets/db-springsession-password)
LOG_PATH=/opt/mypostgres/logs/postgresql.$(date +%F.%Hh%Mm%Ss).log
DATA_PATH=/opt/mypostgres/data
HBA_CONF_PATH=/opt/mypostgres/scripts/pg_hba.conf

# Before PostgreSQL can function correctly, the database cluster must be initialized:
/usr/lib/postgresql/16/bin/initdb -D $DATA_PATH

# internal start of server in order to allow set-up using psql-client
# does not listen on external TCP/IP and waits until start finishes
/usr/lib/postgresql/16/bin/pg_ctl -D $DATA_PATH -l $LOG_PATH -o "-c listen_addresses='*'" -o "-c hba_file='$HBA_CONF_PATH'" -w start

# create a user or role
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "CREATE USER web WITH PASSWORD '$DATABASE_WEB_PASSWORD';"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "CREATE DATABASE web TEMPLATE template0 ENCODING 'UNICODE';"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "ALTER DATABASE web OWNER TO web;"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "GRANT ALL PRIVILEGES ON DATABASE web TO web;"

#set springsession database
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "CREATE USER springsession WITH PASSWORD '$DATABASE_SPRINGSESSION_PASSWORD';"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "CREATE DATABASE springsession TEMPLATE template0 ENCODING 'UNICODE';"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "ALTER DATABASE springsession OWNER TO springsession;"
/usr/lib/postgresql/16/bin/psql -v ON_ERROR_STOP=1 -c "GRANT ALL PRIVILEGES ON DATABASE springsession TO springsession;"

# stop internal postgres server
trap "./scripts/postgres-exitpoint.sh $DATA_PATH" SIGINT SIGTERM
tail -f $LOG_PATH

exec "$@"