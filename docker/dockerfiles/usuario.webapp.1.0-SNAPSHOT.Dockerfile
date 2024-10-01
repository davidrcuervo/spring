FROM myjava:21.0.4

USER root

RUN useradd -g docker -c "Usuario application user" --shell /bin/bash --create-home --home-dir /opt/myusuarioapp usuarioappuser

USER usuarioappuser

WORKDIR /opt/myusuarioapp
ENV HOME=/opt/myusuarioapp

RUN mkdir .ssh
COPY private/keys/. .ssh/.
COPY scripts/webapp/github.ssh.config .ssh/
RUN cat .ssh/github.ssh.config >> .ssh/config

#RUN git clone git@github.com:davidrcuervo/spring.git