#!/bin/bash

DATA_PATH=/opt/mypostgres/data

echo "Stopping PostgreSQL...."
/usr/lib/postgresql/16/bin/pg_ctl -D "$DATA_PATH" -m fast -w stop
echo "Closed, good bye!!"