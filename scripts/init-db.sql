-- PostgreSQL initialization script for Docker
-- Creates separate schemas/databases for each microservice

CREATE DATABASE userdb;
CREATE DATABASE productdb;
CREATE DATABASE orderdb;

\c userdb;
GRANT ALL PRIVILEGES ON DATABASE userdb TO asfaw;

\c productdb;
GRANT ALL PRIVILEGES ON DATABASE productdb TO asfaw;

\c orderdb;
GRANT ALL PRIVILEGES ON DATABASE orderdb TO asfaw;
