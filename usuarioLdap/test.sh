#!/bin/bash
JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password)
DIR=$(pwd)
mvn test \
-Djasypt.encryptor.password=$JASYPT_PASSWORD \
-Dspring.config.additional-location=$DIR/spring/API/,$DIR/spring/ \
-f spring/usuario/pom.xml