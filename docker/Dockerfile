FROM ubuntu:24.04 AS myubuntu

#Variables and arguments
#ARG ADMUSER_PASSWORD=secret
#ARG SAMSEPI0L_PASSWORD=secret

#update ubuntu os
RUN apt update
RUN apt upgrade -y

#install required packages
RUN apt install -y openssl sudo openssh-client

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

USER samsepi0l
ENV HOME=/home/sapsepi0l

################################
## MY POSTGRESQL IMAGE
###############################
FROM myubuntu AS mypostgres

ENV DEBIAN_FRONTEND=noninteractive
ARG POSTGRES_UID=1001
ARG POSTGRES_GID=1001

USER root

#Create postgres user
RUN groupadd -g $POSTGRES_GID postgres
RUN useradd -u $POSTGRES_UID -g postgres -c "PostgreSQL Administrator" --shell /bin/bash --home-dir /opt/postgresql postgres

#create app directory
WORKDIR /opt/mypostgres
RUN chown postgres:postgres /opt/mypostgres
RUN sudo -u postgres mkdir -m 700 data
RUN sudo -u postgres mkdir -m 755 logs

#Install postgres
RUN apt install -y postgresql

#copy files, scripts, etc
COPY ../scripts/database/. scripts/.
RUN chmod 755 -R scripts/

#Create databases
#RUN scripts/createdb.sh

#Run
USER postgres
#ENTRYPOINT ["/bin/bash"]
ENTRYPOINT ["scripts/postgres-entrypoint.sh"]

################################
## MY OpenLDAP IMAGE (2.6.7)
###############################
FROM myubuntu AS myslapd

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
## MY Java IMAGE (jdk: 21.0.2 & mvn: 3.9.9)
############################################
FROM myubuntu AS myjava

#Arguments and variables
ARG DOCKER_GID=9999

USER root

#Create java user and docker group to be able to save data externally
RUN groupadd --gid $DOCKER_GID docker
RUN useradd -g docker -c "Java user" --shell /bin/bash --create-home --home-dir /opt/myjava javauser

#install git
RUN apt install -y git

#create directory and set permissions
WORKDIR /opt/myjava
#RUN chown javauser:docker /opt/myjava && chmod 750 /opt/myjava

#copy keys to authenticate with external servers
RUN sudo -u javauser -g docker mkdir -m 0750 .ssh
COPY private/keys/. .ssh/.
RUN chown javauser:docker -R .ssh && chmod 700 -R .ssh

USER javauser

#get software from software repository server
RUN scp -i /opt/myjava/.ssh/docker.key -o StrictHostKeyChecking=no myself@homeServer3.la-etienda.com:/home/myself/Downloads/Software/Java/jdk-21_linux-x64_bin.tar.gz /opt/myjava/
RUN scp -i /opt/myjava/.ssh/docker.key -o StrictHostKeyChecking=no myself@homeServer3.la-etienda.com:/home/myself/Downloads/Software/Java/apache-maven-3.9.9-bin.tar.gz /opt/myjava/

#extract files
RUN tar -xzvf jdk-21_linux-x64_bin.tar.gz
RUN rm jdk-21_linux-x64_bin.tar.gz
RUN tar -xzvf apache-maven-3.9.9-bin.tar.gz
RUN rm apache-maven-3.9.9-bin.tar.gz

#set Java environment variables
ENV JAVA_HOME=/opt/myjava/jdk-21.0.4
ENV M2_HOME=/opt/myjava/apache-maven-3.9.9
ENV M2=$M2_HOME/bin
ENV PATH="$JAVA_HOME/bin:$M2:$PATH"

############################################
## MY webapp IMAGE
############################################
FROM myjava AS webapp

USER root

RUN useradd -g docker -c "Usuario application user" --shell /bin/bash --create-home --home-dir /opt/webapp webappuser

USER webappuser

#Set application folder
WORKDIR /opt/webapp
ENV HOME=/opt/webapp

#Set ssh to be able to connect to github
RUN mkdir .ssh
COPY private/keys/. .ssh/.
COPY scripts/webapp/github.ssh.config .ssh/
RUN cat .ssh/github.ssh.config >> .ssh/config

USER root
RUN chown webappuser:docker -R .ssh && chmod 750 -R .ssh

USER webappuser
RUN chmod 700 .ssh/docker.key

#Clone the application
RUN git clone github.com:davidrcuervo/spring.git

#Clean project through maven
RUN mvn clean -f spring/pom.xml
RUN mvn install -f spring/library/pom.xml
RUN mvn install -f spring/model/pom.xml
RUN mvn install -f spring/utils/pom.xml

############################################
## MY messengerapp IMAGE
############################################
FROM webapp AS messengerapp

#Set application folder
WORKDIR /opt/webapp
ENV HOME=/opt/webapp

ENTRYPOINT ["/bin/bash", "-c"]
CMD ["JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password) && mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Djasypt.encryptor.password=$JASYPT_PASSWORD -f spring/messenger/pom.xml"]

############################################
## MY usuarioapp IMAGE
############################################
FROM webapp AS usuarioapp

#Set application folder
WORKDIR /opt/webapp
ENV HOME=/opt/webapp

ENTRYPOINT ["/bin/bash", "-c"]
CMD ["JASYPT_PASSWORD=$(cat /run/secrets/jasypt-password) && mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Djasypt.encryptor.password=$JASYPT_PASSWORD -f spring/usuario/pom.xml"]
