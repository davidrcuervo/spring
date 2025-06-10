#!/usr/bin/env bash

METHOD=POST
#PARAMETERS_REQUIRED:
#PARAMETERS_OPTIONAL:
#REQUEST_BODY: USER{username, password}
#RESPONSE_BODY: GROUP_LIST
#AUTHORIZATION: ANY

set -o xtrace
USERNAME=$1
PASSWORD=$2
REQUEST=http://127.0.0.1:8081/api/v0/user/authenticate.html

RESPONSE_CODE=$(curl -i --request $METHOD --header "Content-Type: application/json" \
--write-out "%{http_code}" --output .api.output \
--data '{
"username":"'$USERNAME'",
"password":"'$PASSWORD'"
}' \
$REQUEST)

cat .api.output

if [[ "$RESPONSE_CODE" -eq "200" ]]
then
  RESULT=0
else
  RESULT=$RESPONSE_CODE
fi

#echo $RESULT
exit $RESULT