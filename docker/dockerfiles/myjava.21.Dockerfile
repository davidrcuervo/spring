FROM myubuntu:24.04

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