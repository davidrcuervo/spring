# Project Overview

This is a multi-module Spring Boot web application designed with a microservice architecture, containerized using Docker and Docker Compose. It includes various services for user management, schema handling, messaging, and a frontend, all secured with Keycloak and backed by a PostgreSQL database. Nginx acts as a reverse proxy and load balancer.

# Key Technologies

*   **Backend:** Java 25, Spring Boot, Maven
*   **Containerization:** Docker, Docker Compose
*   **Identity & Access Management:** Keycloak 26.5.5, OpenLDAP
*   **Database:** PostgreSQL 16.4
*   **Web Server/Proxy:** Nginx 1.24
*   **Security:** Jasypt (for encryption), OpenSSL (for certificates)
*   **Frontend:** (Details to be determined, but likely a web framework integrated with Spring Boot)
*   **Build Tool:** Maven 3.9.14
*   **Testing:** JUnit 6.0.3

# Architecture

The application follows a microservice pattern, with several independent Spring Boot services communicating over a backend network. Key components include:
*   **`postgreset`**: PostgreSQL database service.
*   **`keycloaket`**: Keycloak identity provider.
*   **`etnginx`**: Nginx reverse proxy, handling external traffic and routing to internal services.
*   **`etuser`**: User management service (Spring Boot).
*   **`etschema`**: Schema management service (Spring Boot).
*   **`etmail`**: Messaging service (Spring Boot).
*   **`frontend`**: Web frontend application (Spring Boot).

# Development Environment

The project is designed to be run within Docker containers. The `docker-compose.yml` file orchestrates all services.

## Prerequisites
*   Docker
*   Docker Compose
*   JDK 25 (for local development/compilation outside Docker, though Docker handles this)
*   Maven 3.9.14 (for local development/compilation outside Docker)

## Setup and Installation
1.  **Clone the repository:** `git clone git@github.com:davidrcuervo/webapp.git`
2.  **Create private folders and keys:** Refer to `readme.org` for details on setting up `docker/private/keys` and `docker/private` directories.
3.  **Download software:** Place required software (JDK, Maven, Jasypt, Keycloak, JUnit) into `docker/Software` as specified in `readme.org`.
4.  **Create passwords:** Generate and store passwords in `admuser-password.txt`, `samsepi0l-password.txt`, and `jasypt-password.txt` within `docker/private`.
5.  **Edit environment file:** Configure `.env` in the project root.
6.  **Build Docker images:** `docker compose build --no-cache --build-arg DOCKER_GID=<your_docker_gid>` (replace `<your_docker_gid>` with your Docker group ID, found using `cat /etc/group | grep docker`).

## Running the Application
Refer to the "Test and Run" section in `readme.org` for detailed steps, including starting database and Keycloak, and then configuring Keycloak before launching other services.

# Important Directories and Files

*   `/home/node/webapp/`: Project root.
*   `/home/node/webapp/pom.xml`: Main Maven parent POM.
*   `/home/node/webapp/Dockerfile`: Multi-stage Dockerfile for building various service images.
*   `/home/node/webapp/docker-compose.yml`: Defines and orchestrates all Docker services.
*   `/home/node/webapp/application.yml`: Spring Boot application configuration.
*   `/home/node/webapp/readme.org`: Project documentation, setup instructions, and API details.
*   `/home/node/webapp/API/`: Contains API definitions or a core API module.
*   `/home/node/webapp/frontend/`: Frontend application module.
*   `/home/node/webapp/userKc/`: User Keycloak integration module.
*   `/home/node/webapp/schema/`: Schema service module.
*   `/home/node/webapp/messenger/`: Messaging service module.
*   `/home/node/webapp/library/`, `/home/node/webapp/model/`, `/home/node/webapp/utils/`: Shared library modules.
*   `/home/node/webapp/docker/private/`: Contains sensitive files like passwords and private keys.
*   `/home/node/webapp/docker/scripts/`: Contains various shell scripts for Docker entrypoints and configurations.

# Coding Conventions

*   **Language:** Java for backend services.
*   **Build System:** Maven.
*   **Spring Boot:** Adhere to Spring Boot best practices for application development.
*   **Docker:** Follow Docker best practices for image creation and container orchestration.
*   **Security:** Prioritize secure coding practices, especially concerning password handling and certificate management.

# Testing

*   **JUnit:** Used for unit and integration testing within Java modules.
*   **Docker Compose Healthchecks:** Services include health checks to ensure proper startup and functionality within the Docker environment.
*   **Running Tests:** Refer to `readme.org` for instructions on running JUnit tests, including required system properties for Jasypt encryption.

# Deployment

The application is deployed using Docker Compose, which manages the lifecycle of all services.

# Security

*   **Jasypt:** Used for encrypting sensitive properties in configuration files.
*   **Self-signed Certificates:** Used for secure communication between services (e.g., Keycloak, Nginx).
*   **Password Management:** Passwords are stored in encrypted files and managed securely within the Docker environment.
*   **User Management:** Keycloak and OpenLDAP are used for robust identity and access management.
