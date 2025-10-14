FROM ubuntu:24.04 AS ubuntuet

#update ubuntu os
RUN apt update
RUN apt upgrade -y

#install required packages
RUN apt install -y openssl sudo openssh-client curl dos2unix gettext-base netcat-openbsd

#copy private files
WORKDIR /opt/docker
RUN mkdir private
RUN chmod 700 private
COPY docker/private/admuser-password.txt private/
COPY docker/private/samsepi0l-password.txt private/

#copy envrinment file
COPY .env webapp.env
RUN dos2unix webapp.env
RUN chmod 740 webapp.env

#Create administrator user
RUN ADMUSER_CYPHERED_PASSWORD=$(cat private/admuser-password.txt | openssl passwd -1 -stdin) && \
    useradd -c "Administrator user" --password "$ADMUSER_CYPHERED_PASSWORD" --shell /bin/bash --create-home --home-dir /home/admuser admuser
RUN usermod -a -G sudo admuser

#Create a simple user
RUN SAMSEPI0L_CYPHERED_PASSWORD=$(cat private/samsepi0l-password.txt | openssl passwd -1 -stdin) && \
    useradd -c "Simple user." --password "$SAMSEPI0L_CYPHERED_PASSWORD" --shell /bin/bash --create-home --home-dir /home/samsepi0l samsepi0l

#remove sesitive files from image
RUN rm private/admuser-password.txt
RUN rm private/samsepi0l-password.txt

############################################
## MY Java IMAGE (jdk: 21.0.6 & mvn: 3.9.9)
############################################
FROM ubuntuet AS javaet

USER root

#Create java user and docker group to be able to save data externally
RUN export DOCKER_GID=$(cat /opt/docker/webapp.env | grep DOCKER_GID | awk -F "=" '{print $2}') && \
    groupadd --gid $DOCKER_GID docker
RUN useradd -g docker -c "Java user" --shell /bin/bash --create-home --home-dir /opt/myjava javauser

#install git
RUN apt install -y git unzip

#create directory and set permissions
WORKDIR /opt/myjava
#RUN chown javauser:docker /opt/myjava && chmod 750 /opt/myjava

#copy keys to authenticate with external servers
RUN sudo -u javauser -g docker mkdir -m 0750 .ssh
RUN mkdir -m 0750 /opt/jasypt
RUN chown javauser:docker /opt/jasypt
#COPY private/keys/. .ssh/.
RUN chown javauser:docker -R .ssh && chmod 700 -R .ssh

#get software from software repository server
#RUN scp -i /opt/myjava/.ssh/docker.key -o StrictHostKeyChecking=no myself@homeServer3.la-etienda.com:/home/myself/Downloads/Software/Java/jdk-21.0.4_linux-x64_bin.tar.gz /opt/myjava/
COPY docker/Software/jdk-21.0.6_linux-x64_bin.tar.gz /opt/myjava/
#RUN scp -i /opt/myjava/.ssh/docker.key -o StrictHostKeyChecking=no myself@homeServer3.la-etienda.com:/home/myself/Downloads/Software/Java/apache-maven-3.9.9-bin.tar.gz /opt/myjava/
COPY docker/Software/apache-maven-3.9.9-bin.tar.gz /opt/myjava/
#RUN scp -i /opt/myjava/.ssh/docker.key -o StrictHostKeyChecking=no myself@homeServer3.la-etienda.com:/home/myself/Downloads/Software/jasypt/jasypt-1.9.3-dist.zip /opt/jasypt
COPY docker/Software/jasypt-1.9.3-dist.zip /opt/jasypt/
COPY docker/scripts/jasypt/jdecrypt.sh /opt/jasypt/
RUN chmod 750 .ssh
RUN chmod 755 /opt/jasypt/jdecrypt.sh

#extract files
RUN unzip /opt/jasypt/jasypt-1.9.3-dist.zip -d /opt/jasypt
RUN rm /opt/jasypt/jasypt-1.9.3-dist.zip
RUN chmod +x -R /opt/jasypt/jasypt-1.9.3/bin
RUN tar -xzvf jdk-21.0.6_linux-x64_bin.tar.gz
RUN rm jdk-21.0.6_linux-x64_bin.tar.gz
RUN tar -xzvf apache-maven-3.9.9-bin.tar.gz
RUN rm apache-maven-3.9.9-bin.tar.gz

#set Java environment variables
ENV JAVA_HOME=/opt/myjava/jdk-21.0.6
ENV M2_HOME=/opt/myjava/apache-maven-3.9.9
ENV M2=$M2_HOME/bin
ENV PATH="$JAVA_HOME/bin:$M2:/opt/jasypt/jasypt-1.9.3/bin:$PATH"

#import self signed certificates in java
RUN mkdir certs
RUN chown javauser:docker certs && chmod 750 certs
COPY docker/private/keys/*.crt certs
RUN chmod 744 certs/*.crt
COPY docker/private/keys/*.key certs
RUN chmod 700 certs/*.key
RUN keytool -import -trustcacerts -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit -noprompt -alias keycloak -file certs/kc.crt
RUN keytool -import -trustcacerts -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit -noprompt -alias webapp -file certs/webapp.crt

#compile jasypt decrypt
#RUN mvn clean -f /opt/jasypt/pom.xml
#RUN mvn package -f /opt/jasypt/pom.xml

################################
## MY POSTGRESQL IMAGE
###############################
FROM javaet AS postgreset

ARG POSTGRES_UID=1001
ARG POSTGRES_GID=1001
ENV DEBIAN_FRONTEND=noninteractive

USER root

#Create postgres user
RUN groupadd -g $POSTGRES_GID postgres
RUN useradd -u $POSTGRES_UID -g postgres -c "PostgreSQL Administrator" --shell /bin/bash --home-dir /opt/postgresql postgres
RUN usermod -a -G docker postgres

#create app directory
WORKDIR /opt/mypostgres

RUN chown postgres:postgres /opt/mypostgres
RUN sudo -u postgres mkdir -m 700 data
RUN sudo -u postgres mkdir -m 755 logs
RUN sudo -u postgres mkdir -m 775 .m2

#Install postgres
RUN apt install -y postgresql

#copy files, scripts, etc
COPY docker/scripts/database/. scripts/.
RUN chown postgres:postgres -R scripts/
RUN chmod 755 -R scripts/

################################
## MY NGINX IMAGE
###############################
FROM javaet AS etnginx

USER root

#install nginx
RUN apt install -y nginx

RUN useradd -g docker -c "Nginx application user" --shell /bin/bash --create-home --home-dir /opt/nginx nginx
WORKDIR /opt/nginx

#copy cartificates and keys

#COPY ./private/keys/kc.crt .
#RUN chmod 744 kc.crt
#COPY ./private/keys/kc.unsecure.key .
#RUN chmod 700 kc.unsecure.key
#COPY ./private/keys/kc.private.key .
#RUN chmod 700 kc.private.key
#COPY ./private/keys/webapp.crt .
#RUN chmod 744 webapp.crt
#COPY ./private/keys/webapp.unsecure.key .
#RUN chmod 700 webapp.unsecure.key
#COPY ./private/keys/webapp.private.key .
#RUN chmod 700 webapp.private.key

#copy sites and enable them
COPY docker/scripts/nginx .
RUN find ./ -maxdepth 1 -type f -name "*.sh" | xargs dos2unix
RUN chmod 740 nginx.entrypoint.sh

############################################
## Keycloak
############################################
FROM javaet AS keycloaket

USER root

RUN useradd -g docker -c "Keycloak application user" --shell /bin/bash --create-home --home-dir /opt/keycloak keycloak

#RUN scp -i /opt/myjava/.ssh/docker.key -o StrictHostKeyChecking=no myself@homeServer3.la-etienda.com:/home/myself/Downloads/Software/keycloak/keycloak-26.0.5.zip /opt/keycloak
COPY docker/Software/keycloak-26.1.5.zip /opt/keycloak

WORKDIR /opt/keycloak
COPY docker/scripts/keycloak .
RUN find ./ -maxdepth 1 -type f -name "*.sh" | xargs dos2unix

#copy certificates and keys
RUN chown keycloak:docker /opt/myjava/certs/kc.unsecure.key
#RUN find ./ -maxdepth 1 -type f -name "kc.*" | xargs chown keycloak:docker

USER keycloak

RUN unzip keycloak-26.1.5.zip
RUN rm keycloak-26.1.5.zip

ENV HOME=/opt/keycloak
ENV PATH="$HOME/keycloak-26.1.5/bin:$PATH"

############################################
## Keycloak Configuration
############################################
FROM keycloaket AS keycloaketcnf

USER root

RUN apt install -y vim

############################################
## MY webapp IMAGE
############################################
FROM javaet AS etapp

USER root

RUN useradd -g docker -c "Usuario application user" --shell /bin/bash --create-home --home-dir /opt/webapp webappuser

USER webappuser
STOPSIGNAL SIGINT

#Set application folder
WORKDIR /opt/webapp
ENV HOME=/opt/webapp

#Set ssh to be able to connect to github
#RUN mkdir .ssh
#COPY private/keys/. .ssh/.
#COPY scripts/webapp/github.ssh.config .ssh/config
#RUN chown webappuser:docker -R .ssh && chmod 750 -R .ssh
#RUN chmod 700 .ssh/docker.key
#RUN git clone github.com:davidrcuervo/spring.git

RUN mkdir -m 750 src
RUN mkdir -m 750 target
RUN mkdir -m 750 API
RUN mkdir -m 750 bin
RUN mkdir -m 750 etc

COPY --chmod=0750 --chown=webappuser:docker API/. API/.
COPY --chmod=0750 --chown=webappuser:docker application.yml etc/.
COPY --chmod=0750 --chown=webappuser:docker docker/scripts/webapp/. bin/.

#USER root
#USER webappuser

#Clone the application
RUN --mount=type=bind,source=library,target=src/library bin/compile.sh library
RUN --mount=type=bind,source=model,target=src/model bin/compile.sh model
RUN --mount=type=bind,source=utils,target=src/utils bin/compile.sh utils

#Clean project through maven
#RUN mvn clean -f spring/pom.xml
#RUN mvn install -f spring/library/pom.xml
#RUN mvn install -f spring/model/pom.xml
#RUN mvn install -f spring/utils/pom.xml
#RUN mvn install -DskipTests -f spring/userKc/pom.xml
#RUN mvn install -DskipTests -f spring/frontend/pom.xml
#RUN mvn package -DskipTests -f spring/webapp-test/pom.xml

############################################
## MY usuarioapp IMAGE
############################################
FROM etapp AS testcontainer

RUN --mount=type=bind,source=userKc,target=src/userKc bin/compile.sh userKc
#RUN chmod 744 spring/usuario/test.sh
#RUN chmod 744 spring/schema/test.sh

################################
## MY OpenLDAP IMAGE (2.6.7)
###############################
FROM ubuntuet AS myslapd

#variables and arguments
ENV DEBIAN_FRONTEND=noninteractive
ARG OPENLDAP_UID=1001
ARG OPENLDAP_GID=1001

USER root
WORKDIR /opt/myslapd

#Create openldap user
RUN groupadd -g $OPENLDAP_GID openldap
RUN useradd -u $OPENLDAP_UID -g openldap -c "OpenLDAP Server Account" --shell /bin/false --home-dir /opt/myslapd openldap

#install openldap
RUN echo 'slapd/root_password password password' | debconf-set-selections && \
    echo 'slapd/root_password_again password password' | debconf-set-selections && \
    apt install -y slapd ldap-utils
#    /etc/init.d/slapd start

#Copy ldif schemas to docker guest container
RUN mkdir etienda
RUN mkdir etienda/scripts
RUN mkdir etienda/data
COPY docker/scripts/ldap etienda/scripts/
#COPY ./scripts/ldap/etienda.github.ssh.config.ldif etienda/scripts/
#COPY ./scripts/ldap/etienda.ldif etienda/scripts/
RUN chown openldap:openldap -R etienda
RUN chmod 700 -R etienda

#install ldap directory
RUN /etc/init.d/slapd start && \
    ldapmodify -Y EXTERNAL -H ldapi:/// -f /opt/myslapd/etienda/scripts/etienda.config.ldif
#    ldapmodify -Y EXTERNAL -H ldapi:/// -f /opt/myslapd/etienda/scripts/etienda.ldif

#RUN
USER openldap
ENTRYPOINT ["/opt/myslapd/etienda/scripts/slapd-entrypoint.sh"]
#ENTRYPOINT ["/usr/sbin/slapd", "-u", "openldap", "-g", "openldap", "-F", "/etc/ldap/slapd.d", "-h", "ldaps:/// ldapi:/// ldap:///", "-d", "stats"]
#USER samsepi0l
#ENTRYPOINT ["/bin/bash"]