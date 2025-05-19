#!/usr/bin/env bash

function close(){
  echo "Good bye!!"
  exit 0
}

echo "Starting listener on port 8100"

trap close SIGINT SIGQUIT SIGHUP SIGTERM
"$@" &
CHILD_PID=$!
wait "$CHILD_PID"
