#!/bin/bash
JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password)
mvn spring-boot:run \
"-Dspring-boot.run.jvmArguments=-Djasypt.encryptor.password=$JASYPT_PASSWORD \
-Dspring.config.additional-location=$DIR/spring/API/,$DIR/spring/" \
-f spring/messenger/pom.xml