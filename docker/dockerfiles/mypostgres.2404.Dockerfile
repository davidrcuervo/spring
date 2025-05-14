FROM myubuntu:24.04

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