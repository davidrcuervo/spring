#!/bin/bash

DATA_PATH=$1

echo "Stopping PostgreSQL...."
/usr/lib/postgresql/16/bin/pg_ctl -D "$DATA_PATH" -m fast -w stop
echo "Closed!"