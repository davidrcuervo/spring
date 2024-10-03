FROM myjava:21.0.4

USER root

RUN useradd -g docker -c "Usuario application user" --shell /bin/bash --create-home --home-dir /opt/myusuarioapp usuarioappuser

USER usuarioappuser

#Set application folder
WORKDIR /opt/myusuarioapp
ENV HOME=/opt/myusuarioapp

#Set ssh to be able to connect to github
RUN mkdir .ssh
COPY private/keys/. .ssh/.
COPY scripts/webapp/github.ssh.config .ssh/
RUN cat .ssh/github.ssh.config >> .ssh/config

#Clone the application
RUN git clone github.com:davidrcuervo/spring.git

#Clean project through maven
RUN mvn clean -f spring/library/pom.xml
RUN mvn clean -f spring/model/pom.xml
RUN mvn clean -f spring/utils/pom.xml
RUN mvn clean -f spring/messenger/pom.xml
RUN mvn clean -f spring/usuario/pom.xml
RUN mvn clean -f spring/frontend/pom.xml