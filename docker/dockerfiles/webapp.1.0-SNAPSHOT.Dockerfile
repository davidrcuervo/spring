FROM myjava:21.0.4

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