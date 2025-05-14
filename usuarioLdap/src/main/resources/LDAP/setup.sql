--create database to store application data
CREATE USER laetienda WITH PASSWORD 'secret';
CREATE DATABASE laetienda TEMPLATE template0 ENCODING 'UNICODE';
ALTER DATABASE laetienda OWNER TO laetienda;
GRANT ALL PRIVILEGES ON DATABASE laetienda TO laetienda;

--create database to store spring session
CREATE USER springsession WITH PASSWORD 'secret';
CREATE DATABASE springsession TEMPLATE template0 ENCODING 'UNICODE';
ALTER DATABASE springsession OWNER TO springsession;
GRANT ALL PRIVILEGES ON DATABASE springsession TO springsession;