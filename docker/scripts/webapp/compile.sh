#!/bin/bash
echo "Compiling starting..."

DIR="/opt/webapp/src/library"

mvn clean -Pcontainer -f $DIR/pom.xml
mvn install -Pcontainer -f $DIR/pom.xml

echo "...Compiling finished successfully!!!"