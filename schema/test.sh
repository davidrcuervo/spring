#!/bin/bash
JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password)
DIR=$(pwd)
#$DIR/spring/schema/mvn test \
mvn test \
"-Dspring-boot.run.jvmArguments=-Djasypt.encryptor.password=$JASYPT_PASSWORD \
-Dspring.config.additional-location=$DIR/spring/API/,$DIR/spring/" \
-f spring/schema/pom.xml