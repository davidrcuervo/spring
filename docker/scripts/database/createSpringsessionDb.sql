--create database to store spring session
CREATE DATABASE springsession TEMPLATE template0 ENCODING 'UNICODE';
ALTER DATABASE springsession OWNER TO springsession;
GRANT ALL PRIVILEGES ON DATABASE springsession TO springsession;