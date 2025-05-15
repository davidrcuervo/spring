FROM ubuntu:24.04 AS ubuntuet
#Variables and arguments
#ARG ADMUSER_PASSWORD=secret
#ARG SAMSEPI0L_PASSWORD=secret

#update ubuntu os
RUN apt update
RUN apt upgrade -y

#install required packages
RUN apt install -y openssl sudo openssh-client curl dos2unix

#copy private files
WORKDIR /var/docker/build
RUN mkdir private
RUN chmod 700 private
COPY private/admuser-password.txt private/
COPY private/samsepi0l-password.txt private/

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

#USER samsepi0l
#ENV HOME=/home/sapsepi0l

############################################
## MY Java IMAGE (jdk: 21.0.6 & mvn: 3.9.9)
############################################
FROM ubuntuet AS javaet

#Arguments and variables
ARG DOCKER_GID=9999

USER root

#Create java user and docker group to be able to save data externally
RUN groupadd --gid $DOCKER_GID docker
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
COPY Software/jdk-21.0.6_linux-x64_bin.tar.gz /opt/myjava/
#RUN scp -i /opt/myjava/.ssh/docker.key -o StrictHostKeyChecking=no myself@homeServer3.la-etienda.com:/home/myself/Downloads/Software/Java/apache-maven-3.9.9-bin.tar.gz /opt/myjava/
COPY Software/apache-maven-3.9.9-bin.tar.gz /opt/myjava/
#RUN scp -i /opt/myjava/.ssh/docker.key -o StrictHostKeyChecking=no myself@homeServer3.la-etienda.com:/home/myself/Downloads/Software/jasypt/jasypt-1.9.3-dist.zip /opt/jasypt
COPY Software/jasypt-1.9.3-dist.zip /opt/jasypt/
COPY scripts/jasypt/. /opt/jasypt/
RUN chmod 750 .ssh

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

#compile jasypt decrypt
RUN mvn clean -f /opt/jasypt/pom.xml
RUN mvn install -f /opt/jasypt/pom.xml

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
COPY ../scripts/database/. scripts/.
RUN chown postgres:postgres -R scripts/
RUN chmod 755 -R scripts/

################################
## MY POSTGRESQL IMAGE
###############################
FROM javaet AS etnginx

USER root

#install nginx
RUN apt install -y nginx

RUN useradd -g docker -c "Nginx application user" --shell /bin/bash --create-home --home-dir /opt/nginx nginx

#copy sites and enable them
COPY ./scripts/nginx/. /etc/nginx/sites-available/.
RUN ln -s /etc/nginx/sites-available/keycloak.nginx /etc/nginx/sites-enabled/keycloak
RUN ln -s /etc/nginx/sites-available/webapp.nginx /etc/nginx/sites-enabled/webapp

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
COPY ./scripts/ldap/. etienda/scripts/
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

############################################
## Keycloak
############################################
FROM javaet AS keycloaket

USER root

RUN useradd -g docker -c "Keycloak application user" --shell /bin/bash --create-home --home-dir /opt/keycloak keycloak

#RUN scp -i /opt/myjava/.ssh/docker.key -o StrictHostKeyChecking=no myself@homeServer3.la-etienda.com:/home/myself/Downloads/Software/keycloak/keycloak-26.0.5.zip /opt/keycloak
COPY Software/keycloak-26.1.5.zip /opt/keycloak

WORKDIR /opt/keycloak
COPY scripts/keycloak/. .
RUN find ./ -maxdepth 1 -type f -name "*.sh" | xargs dos2unix

USER keycloak

RUN unzip keycloak-26.1.5.zip
RUN rm keycloak-26.1.5.zip
#RUN chmod g+r -R /opt/keycloak/keycloak-26.0.5
#RUN chmod g+x -R /opt/keycloak/keycloak-26.0.5/bin

ENV HOME=/opt/keycloak
ENV PATH="$HOME/keycloak-26.1.5/bin:$PATH"

############################################
## Keycloak Configuration
############################################
FROM keycloaket AS keycloaketcnf

USER root

RUN apt install -y vim netcat-openbsd

############################################
## MY webapp IMAGE
############################################
FROM javaet AS etapp

USER root

RUN useradd -g docker -c "Usuario application user" --shell /bin/bash --create-home --home-dir /opt/webapp webappuser

USER webappuser

#Set application folder
WORKDIR /opt/webapp
ENV HOME=/opt/webapp

#Set ssh to be able to connect to github
RUN mkdir .ssh
COPY private/keys/. .ssh/.
COPY scripts/webapp/github.ssh.config .ssh/config
#RUN #cat .ssh/github.ssh.config >> .ssh/config

USER root
RUN chown webappuser:docker -R .ssh && chmod 750 -R .ssh

USER webappuser
RUN chmod 700 .ssh/docker.key

#Clone the application
RUN git clone github.com:davidrcuervo/spring.git
RUN chmod 744 spring/start.sh

#Clean project through maven
RUN mvn clean -f spring/pom.xml
RUN mvn install -f spring/library/pom.xml
RUN mvn install -f spring/model/pom.xml
RUN mvn install -f spring/utils/pom.xml
RUN mvn install -DskipTests -f spring/webapp-test/pom.xml

############################################
## MY messengerapp IMAGE
############################################
FROM etapp AS messengeret

#Set permissions to start script
RUN chmod 744 spring/messenger/start.sh

############################################
## MY usuarioapp IMAGE
############################################
FROM etapp AS etUser

RUN chmod 744 spring/userKc/start.sh

############################################
## MY usuarioapp IMAGE
############################################
FROM etapp AS testcontainer

RUN chmod 744 spring/usuario/test.sh
RUN chmod 744 spring/schema/test.sh