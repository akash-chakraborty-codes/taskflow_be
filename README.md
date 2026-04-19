# TaskFlow v3

A RESTful task/ticket management backend built with **Spring Boot 3**, featuring JWT authentication, role-based access control, OTP-based flows, email notifications, file attachments, and a full commenting system.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Database Setup](#database-setup)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Default Users](#default-users)
- [Profiles](#profiles)

---

## Overview

TaskFlow v3 is the third iteration of a ticket management system. It allows users to create and manage support/task tickets, add comments with file attachments, and track ticket progress through a defined lifecycle. An admin role provides elevated access for user and ticket management across the system.

---

## Tech Stack

| Layer         | Technology                                  |
|---------------|---------------------------------------------|
| Language      | Java 17                                     |
| Framework     | Spring Boot 3.5.6                           |
| Security      | Spring Security + JWT (jjwt 0.11.5)        |
| Persistence   | Spring Data JPA + Hibernate                 |
| Database      | MySQL 8                                     |
| Email         | Spring Mail (Gmail SMTP)                    |
| API Docs      | SpringDoc OpenAPI / Swagger UI 2.6.0        |
| Validation    | Jakarta Bean Validation                     |
| Boilerplate   | Lombok                                      |
| Build Tool    | Maven (with Maven Wrapper)                  |

---

## Project Structure

```
src/main/java/com/jbs/tfv3/
├── config/
│   ├── GlobalCorsConfig.java       # CORS configuration
│   ├── OpenAPIConfig.java          # Swagger/OpenAPI setup
│   └── SecurityConfig.java         # Spring Security filter chain
├── controller/
│   ├── AuthController.java         # Auth, user, OTP endpoints
│   ├── TicketController.java       # Ticket CRUD endpoints
│   └── CommentController.java      # Comment + file upload endpoints
├── dto/                            # Request/Response data transfer objects
├── entity/
│   ├── UserDtls.java               # User entity (ROLE_ADMIN / ROLE_USER)
│   ├── Ticket.java                 # Ticket entity
│   ├── TicketStatus.java           # Enum: NEW, OPEN, IN_PROGRESS, RESOLVED, CLOSED, PENDING
│   ├── Comment.java                # Comment entity
│   ├── Otp.java                    # OTP entity for email verification
│   └── BlacklistedToken.java       # Revoked JWT tokens
├── exception/
│   ├── ResourceNotFoundException.java
│   └── TooManyRequestsException.java
├── filter/
│   └── JwtFilter.java              # JWT validation on each request
├── repository/                     # Spring Data JPA repositories
├── security/
│   └── ExtendedUserDetails.java    # Custom UserDetails implementation
├── service/
│   ├── impl/                       # Service implementations
│   └── *.java                      # Service interfaces
└── Taskflowv3Application.java      # Main application entry point

src/main/resources/
├── application.properties          # Base configuration (active profile, JWT, mail)
├── application-dev.properties      # Dev profile (port 8080, local DB)
├── application-prod.properties     # Production profile
├── application-uat.properties      # UAT profile
└── data.sql                        # Seed data (default admin + user)
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+ (or use the included `./mvnw` wrapper)
- MySQL 8 server running locally

### Database Setup

1. Create the development database:
   ```sql
   CREATE DATABASE taskflowDEV;
   ```

2. The application uses `spring.jpa.hibernate.ddl-auto=validate`, so the schema must already exist. Run your schema migrations/scripts before startup.

3. Seed data is provided in `src/main/resources/data.sql` and will insert default users on first run.

### Configuration

The active profile is set in `application.properties`:

```properties
spring.profiles.active=DEV
```

Edit `application-dev.properties` for local settings:

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/taskflowDEV
file.upload-dir=D:/storage           # Change to a valid path on your machine
```

Edit the base `application.properties` for:
- **JWT Secret** — `jwt.secret`
- **Mail credentials** — `spring.mail.username` / `spring.mail.password`
- **Log file path** — `logging.file.name`

> ⚠️ **Important:** The `application.properties` file currently contains a hardcoded JWT secret and Gmail app password. These should be moved to environment variables or a secrets manager before deploying to any shared environment.

### Running the Application

Using the Maven wrapper:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Or build a JAR and run it:

```bash
./mvnw clean package
java -jar target/taskflowv3-0.0.1-SNAPSHOT.jar
```

---

## API Documentation

Once the application is running, the interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

The raw OpenAPI spec is available at:

```
http://localhost:8080/v3/api-docs
```

---

## API Endpoints

### Auth & User Management (`/api`)

| Method | Endpoint                  | Access        | Description                        |
|--------|---------------------------|---------------|------------------------------------|
| GET    | `/api/dashboard`          | ADMIN, USER   | Dashboard health check             |
| POST   | `/api/auth/login`         | Public        | Login and receive JWT token        |
| POST   | `/api/users`              | Public        | Register a new user                |
| POST   | `/api/otps`               | Public        | Generate and send OTP via email    |
| GET    | `/api/users`              | ADMIN         | List all users                     |
| PUT    | `/api/users`              | ADMIN, USER   | Update user details                |
| DELETE | `/api/users`              | ADMIN         | Delete a user                      |
| POST   | `/api/logout`             | Authenticated | Invalidate JWT (token blacklist)   |

### Tickets (`/api/tickets`)

| Method | Endpoint                              | Access        | Description                        |
|--------|---------------------------------------|---------------|------------------------------------|
| POST   | `/api/tickets/user/{user_email}`      | ADMIN, USER   | Create a ticket for a user         |
| GET    | `/api/tickets`                        | ADMIN         | Get all tickets                    |
| GET    | `/api/tickets/user/{user_email}`      | ADMIN, USER   | Get all tickets for a user         |
| GET    | `/api/tickets/{ticket_id}`            | ADMIN, USER   | Get a single ticket by ID          |
| PATCH  | `/api/tickets/{ticket_id}`            | ADMIN, USER   | Update ticket details              |
| PATCH  | `/api/tickets/{ticket_id}/status`     | ADMIN         | Update ticket status               |
| DELETE | `/api/tickets/{ticket_id}`            | ADMIN         | Delete a ticket                    |

### Comments (`/api`)

| Method | Endpoint                                   | Access        | Description                        |
|--------|--------------------------------------------|---------------|------------------------------------|
| POST   | `/api/tickets/{ticketId}/comments`         | ADMIN, USER   | Add a comment (with optional file) |
| GET    | `/api/comments/ticket/{ticketId}`          | Public        | Get all comments for a ticket      |
| DELETE | `/api/comments/{commentId}`               | ADMIN, USER   | Delete a comment                   |

---

## Security

- **Stateless JWT authentication** — no server-side session. Every request must include a valid `Authorization: Bearer <token>` header.
- **Token blacklisting** — logout revokes the token by storing it in the `BlacklistedToken` table; the `JwtFilter` checks this on every request.
- **Role-based access** enforced via `@PreAuthorize` annotations (`ROLE_ADMIN` / `ROLE_USER`).
- **OTP flow** — email-based one-time passwords for verification flows, with rate limiting (`TooManyRequestsException`).
- **BCrypt** password hashing.
- **CORS** globally configured via `GlobalCorsConfig`.

---

## Default Users

The `data.sql` seed file creates two users on startup:

| Email                        | Password | Role       |
|------------------------------|----------|------------|
| jayanta.b.sen@yopmail.com    | 1234$4321 | ROLE_ADMIN |
| sujoy.sen@yopmail.com        | 1234$4321 | ROLE_USER  |

> Passwords are BCrypt-encoded in the database. The plaintext above is for development/testing only.

---

## Profiles

| Profile | Description                         | DB                  |
|---------|-------------------------------------|---------------------|
| `DEV`   | Local development (default)         | `taskflowDEV`       |
| `UAT`   | User acceptance testing environment | Configured in UAT properties |
| `PROD`  | Production                          | Configured in prod properties |

Switch the active profile in `application.properties`:

```properties
spring.profiles.active=DEV   # or UAT, PROD
```

---

## Ticket Lifecycle

Tickets move through the following statuses:

```
NEW → OPEN → IN_PROGRESS → RESOLVED → CLOSED
                              ↕
                           PENDING
```

Status updates are restricted to `ROLE_ADMIN`.
