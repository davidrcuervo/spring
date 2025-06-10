#!/bin/bash
JASYPT_PASSWORD=6xtehPTxAL5Rj6sH3WrB
DIR=$(pwd)
#$DIR/spring/schema/mvn test \
spring/schema/mvnw test \
"-Dspring-boot.run.jvmArguments=-Djasypt.encryptor.password=$JASYPT_PASSWORD \
-Dspring.config.additional-location=$DIR/spring/API/,$DIR/spring/" \
-f spring/schema/pom.xml