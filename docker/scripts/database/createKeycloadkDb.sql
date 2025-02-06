--create database to store spring session
CREATE DATABASE keycloak TEMPLATE template0 ENCODING 'UNICODE';
ALTER DATABASE keycloak OWNER TO keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;