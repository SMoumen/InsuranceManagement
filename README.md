# Insurance Management Service

## How to Run

This service uses Docker with TestContainers for easy local testing.

### Prerequisites
- Java 21+
- Maven
- Docker Desktop
- SpringBoot 3.2.0+

### Setup

Add `.testcontainers.properties` to your home folder (e.g., `C:/Users/YourName/.testcontainers.properties`):
   ```
   testcontainers.reuse.enable=true
   ```
   This keeps data persistent across restarts.

### Run Locally
- In IntelliJ or terminal: `mvn spring-boot:run -Dspring-boot.run.profiles=dev -f pom.xml`
- Docker will auto-download and run PostgreSQL.


### Testing 
- Run tests: `mvn test`
- Covers unit and integration tests.

## Architecture

Standard Spring Boot microservice:
- **Controllers**: Handle HTTP requests.
- **Services**: Business logic for contracts and clients.
- **Repositories**: JPA for PostgreSQL DB access.
- **Models**: Entities for DB, DTOs for API.
- **Exceptions**: Basic handler for errors.

This service is stateless, therefore it supports vertical and horizontal scaling. 

For the purpose of this exercise I chose TestContainers with Docker to make the local development easier. 
Also so that you won't have to set up a local database yourself when running the service. 
Otherwise, I would have chosen a real database setup, which doesn't run through Test Containers .


## Points to Improve
- **Security**: Add OAuth/JWT or API keys (currently open access).
    
    Handle secrets through Vault (database name,password,hosts)
- **Performance**: Use Java Virtual Threads for heavy loads if needed.
- **Logging** : Add more metrics and logs, then setup Grafana and a log monitoring tool such as Kibana or Splunk. Also improve exception handling.

