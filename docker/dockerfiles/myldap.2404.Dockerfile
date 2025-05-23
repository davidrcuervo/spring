FROM myubuntu:24.04

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