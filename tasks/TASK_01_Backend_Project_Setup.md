# Task 01: Backend Project Setup

## Objective
Create the Spring Boot backend project structure with Maven configuration.

## Working Directory
```
/Users/dangthang/duancanhan/duanmoi/backend
```

## Requirements
1. Spring Boot version: 3.2+
2. Java version: 17
3. Build tool: Maven
4. Group ID: com.worktime
5. Artifact ID: work-time-analytics-backend
6. Package: com.worktime

## Dependencies Required
- spring-boot-starter-web (REST API)
- spring-boot-starter-data-jpa (Database ORM)
- spring-boot-starter-validation (Bean validation)
- postgresql (PostgreSQL driver)
- flyway-core (Database migrations)
- lombok (Reduce boilerplate)
- spring-boot-starter-test (Testing)
- h2 (In-memory DB for testing)

## Project Structure to Create
```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── worktime/
│   │   │           ├── WorkTimeAnalyticsApplication.java
│   │   │           ├── model/              # JPA Entities
│   │   │           ├── repository/         # Spring Data Repositories
│   │   │           ├── service/            # Business Logic
│   │   │           ├── controller/         # REST Controllers
│   │   │           ├── dto/                # Data Transfer Objects
│   │   │           ├── config/             # Spring Configuration
│   │   │           └── util/               # Utilities
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/              # Flyway SQL scripts
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── worktime/
│       └── resources/
│           └── application-test.yml
└── pom.xml
```

## pom.xml Template
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>

    <groupId>com.worktime</groupId>
    <artifactId>work-time-analytics-backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>Work Time Analytics Backend</name>
    <description>Backend service for Personal Work-Time Activity Analytics System</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway Database Migrations -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Boot Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- H2 Database for Testing -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## application.yml Template
```yaml
spring:
  application:
    name: work-time-analytics-backend
  
  profiles:
    active: dev
  
  datasource:
    url: jdbc:postgresql://localhost:5432/worktime_analytics
    username: worktime_user
    password: worktime_pass
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate  # Never auto-create schema, use Flyway
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0

server:
  port: 8080
  error:
    include-message: always
    include-stacktrace: on_param

logging:
  level:
    root: INFO
    com.worktime: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

## application-dev.yml Template
```yaml
spring:
  jpa:
    show-sql: true
  
  datasource:
    url: jdbc:postgresql://localhost:5432/worktime_analytics
    username: worktime_user
    password: worktime_pass

logging:
  level:
    com.worktime: DEBUG
```

## application-test.yml Template
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  flyway:
    enabled: false

logging:
  level:
    com.worktime: DEBUG
```

## Main Application Class
```java
package com.worktime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Work Time Analytics Backend.
 * 
 * This application provides REST APIs for ingesting, processing, and analyzing
 * physical activity data from Samsung Galaxy Watch via Health Connect.
 * 
 * @author Thang
 * @version 1.0.0
 * @since 2026-01-02
 */
@SpringBootApplication
public class WorkTimeAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkTimeAnalyticsApplication.class, args);
    }
}
```

## Success Criteria
- [ ] Project structure created as specified
- [ ] pom.xml with all dependencies
- [ ] application.yml with PostgreSQL configuration
- [ ] Main application class created
- [ ] Project builds successfully: `mvn clean install`
- [ ] Application starts without errors: `mvn spring-boot:run`
- [ ] Health endpoint accessible: `http://localhost:8080/actuator/health`

## Validation Commands
```bash
cd /Users/dangthang/duancanhan/duanmoi/backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# In another terminal, test health endpoint
curl http://localhost:8080/actuator/health
```

## Notes
- PostgreSQL database doesn't need to exist yet; we'll create it later
- Application will fail to start if DB is not available, which is expected at this stage
- We'll add database setup in the next task

## Next Task
After completing this task, proceed to:
- Task 02: Create JPA Entities
- Task 03: Create Database Schema with Flyway

## Reference Documents
- PRD Section 5: Technical Stack
- docs/DEVELOPMENT_CONTEXT.md
