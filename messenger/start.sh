#!/bin/bash
JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password)
mvn test spring-boot:run \
"-Dspring-boot.run.jvmArguments=-Djasypt.encryptor.password=$JASYPT_PASSWORD \
-Dspring.config.additional-location=./spring/API/,./spring/" \
-f spring/messenger/pom.xml