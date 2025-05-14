FROM ubuntu:24.04

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