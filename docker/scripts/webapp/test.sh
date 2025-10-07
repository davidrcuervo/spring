#!/bin/bash

DIR=$(pwd)
DIR_OUTPUT=/usr/local/bin/webapp/userKc

#$DIR/spring/schema/mvn test \
#spring/userKc/mvnw
mvn clean install -X \
-Dproject.build.directory=/usr/local/bin/webapp/userKc \
-f spring/userKc/pom.xml

#"-Dspring-boot.run.jvmArguments=-Djasypt.encryptor.password=$(cat /run/secrets/jasypt-password) \
#-Dspring.config.additional-location=$DIR/spring/API/,$DIR/spring/" \