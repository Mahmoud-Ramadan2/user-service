
# User Service - Doctor Appointment System

The **User Service** is a core microservice in the *Doctor Appointment System*, responsible for handling user registration, authentication, authorization,password reset, and user profile management. It integrates with Eureka for service discovery and uses JWT for secure access control.

---

## ? Tech Stack

- **Java 21**
- **Spring Boot 3.4.4**
- **Spring Data JPA**
- **Spring Security + JWT**
- **MySQL**
- **Eureka Client**
- **JUnit 5 + Mockito**

---

## ? Features

- ? User registration and login
- ? JWT-based authentication and role-based authorization
- ? Email service using SMTP (e.g., for account verification or password reset)
- ? Eureka integration for service discovery
- ? Actuator for monitoring and health checks
- ? Hibernate ORM with automatic schema update

---

## ? Project Structure

```
user-service/
?
??? src/
?   ??? main/
?   ?   ??? java/com/mahmoud/appointmentsystem/user/   # Core business logic
?   ?   ??? resources/
?   ?       ??? application.properties                  # Configuration
?   ??? test/                                           # Unit and integration tests
??? pom.xml
??? README.md
??? x.env
```

---

## ?? Configuration

### `application.properties`

```properties
# Service Identity
All sensitive and dynamic values are managed using a `.env` file.

### ? Sample `.env`

```dotenv
SPRING_APPLICATION_NAME=user-service
SERVER_PORT=8081

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
EUREKA_INSTANCE_PREFERIPADDRESS=true

# MySQL DB
DB_URL=jdbc:mysql://localhost:3306/doctor_appointment
DB_USERNAME=root
DB_PASSWORD=your_secure_password

# JWT
JWT_SECRET_KEY=your_secure_secret
JWT_EXPIRATION=900000

# Mail (e.g. Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true

# JPA and Profile
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true
SPRING_PROFILES_ACTIVE=dev
```

---

## ? Security

The service uses `spring-security` combined with `java-jwt` for:

- Generating secure access tokens after successful login
- Validating tokens for protected routes
- Role-based access control for user/admin

---

## ? Testing

- Uses **JUnit 5** for unit testing
- Uses **Mockito** for mocking service dependencies
- Includes `spring-security-test` for security layer testing

---

## ? Dependencies Overview (from `pom.xml`)

| Purpose                | Library                               |
|------------------------|----------------------------------------|
| Web Framework          | `spring-boot-starter-web`             |
| JPA ORM                | `spring-boot-starter-data-jpa`        |
| Security               | `spring-boot-starter-security`        |
| JWT Authentication     | `com.auth0:java-jwt`                  |
| Service Discovery      | `spring-cloud-starter-netflix-eureka-client` |
| Monitoring             | `spring-boot-starter-actuator`        |
| MySQL DB               | `mysql-connector-j`                   |
| Email Support          | `spring-boot-starter-mail`            |
| Dev Tools              | `spring-boot-devtools`                |
| Testing                | `spring-boot-starter-test`, `mockito`, `junit-jupiter` |

---

## ? Running the Service

### Prerequisites

- Java 21
- Maven
- MySQL running on `localhost:3306` with DB named `doctor_appointment`
- Eureka server running on port `8761`

### Start Order

1. **Eureka Server**
2. **User Service**
3. Other services (e.g., appointment service)

### Run via Maven:

```bash
mvn clean spring-boot:run
```

---

## ? Eureka & Appointment Service Integration

| Service                | Port   |
|------------------------|--------|
| Eureka Server          | `8761` |
| User Service           | `8081` |
| Appointment Service    | `8080` (example) |

All services register with Eureka for load-balanced inter-service communication.

---

## ?? Author

**Mahmoud Ramadan**  
Email: [mahmoudramadan385@gmail.com](mailto:mahmoudramadan385@gmail.com)

---

## ?? License

This project is licensed under a proprietary license. For use or redistribution, contact the author.

---
