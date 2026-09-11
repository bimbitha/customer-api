# Customer API

A RESTful Customer Management API built using Java 21, Spring Boot, Spring Data JPA, Hibernate, PostgreSQL, and Flyway.

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL 18.6
- Flyway
- Postman
- Git / GitHub

## Architecture

The application follows a layered architecture:

```text
Client / Postman
       |
       v
CustomerController
       |
       v
CustomerService
       |
       v
CustomerRepository
       |
       v
JPA / Hibernate
       |
       v
PostgreSQL
```

### Layers

**Controller**

Handles HTTP requests and responses.

**Service**

Contains business logic.

**Repository**

Uses Spring Data JPA to access the database.

**Entity**

Maps Java objects to PostgreSQL tables.

**Flyway**

Manages and versions database schema changes.

---

# Database

Database:

```text
customer_db
```

Database server:

```text
localhost:5432
```

Main table:

```text
customer
```

Current customer columns:

```text
id
name
email
age
phone_number
address
status
customer_type
```

Flyway history table:

```text
flyway_schema_history
```

## Database Configuration

Database credentials are intentionally not stored in Git.

The application uses an environment variable for the PostgreSQL password:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/customer_db
spring.datasource.username=postgres
spring.datasource.password=${DB_POSTGRES_PASSWORD}
```

Set the password in PowerShell before starting the application:

```powershell
$env:DB_POSTGRES_PASSWORD="YOUR_POSTGRES_PASSWORD"
```

Do not commit the actual password to GitHub.

---

# Running the Application

## Prerequisites

Install:

- JDK 21
- PostgreSQL 18.x
- Maven (or use the included Maven Wrapper)
- Git
- Postman

## Verify Java

```powershell
java -version
```

Expected:

```text
Java 21.x
```

## Verify Maven

```powershell
mvn -version
```

Or use the Maven Wrapper included with the project:

```powershell
.\mvnw.cmd -version
```

## Verify PostgreSQL

```powershell
psql --version
```

Expected:

```text
PostgreSQL 18.x
```

---

# Create the Database

Connect to PostgreSQL:

```powershell
psql -U postgres -d postgres -h localhost
```

Create the application database:

```sql
CREATE DATABASE customer_db;
```

Connect to it:

```sql
\c customer_db
```

---

# Configure the Database Password

In PowerShell:

```powershell
$env:DB_POSTGRES_PASSWORD="YOUR_POSTGRES_PASSWORD"
```

Verify that the variable is set without displaying the password:

```powershell
if ($env:DB_POSTGRES_PASSWORD) {
    "DB_POSTGRES_PASSWORD is SET"
} else {
    "DB_POSTGRES_PASSWORD is NOT SET"
}
```

---

# Build the Application

From the project root:

```powershell
.\mvnw.cmd clean package
```

Expected result:

```text
BUILD SUCCESS
```

---

# Run the Application

```powershell
.\mvnw.cmd spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

---

# REST APIs

## 1. Create Customer

**POST**

```text
/api/customers
```

Full URL:

```text
http://localhost:8080/api/customers
```

Request:

```json
{
  "name": "Anil Kumar",
  "email": "anil.kumar@example.com",
  "age": 35,
  "phoneNumber": "9876543210",
  "address": "Bengaluru, Karnataka",
  "status": "ACTIVE",
  "customerType": "PREMIUM"
}
```

---

## 2. Get All Customers

**GET**

```text
/api/customers
```

Full URL:

```text
http://localhost:8080/api/customers
```

Example response:

```json
[
  {
    "id": 1,
    "name": "Anil Kumar",
    "email": "anil.kumar@example.com",
    "age": 35,
    "phoneNumber": "9876543210",
    "address": "Bengaluru, Karnataka",
    "status": "ACTIVE",
    "customerType": "PREMIUM"
  }
]
```

---

## 3. Get Customer by ID

**GET**

```text
/api/customers/{id}
```

Example:

```text
http://localhost:8080/api/customers/1
```

---

## 4. Update Customer

**PUT**

```text
/api/customers/{id}
```

Example:

```text
http://localhost:8080/api/customers/1
```

Request:

```json
{
  "name": "Anil Kumar",
  "email": "anil.kumar.updated@example.com",
  "age": 36,
  "phoneNumber": "9876543210",
  "address": "Bengaluru, Karnataka",
  "status": "ACTIVE",
  "customerType": "PREMIUM"
}
```

---

## 5. Delete Customer

**DELETE**

```text
/api/customers/{id}
```

Example:

```text
http://localhost:8080/api/customers/1
```

---

## 6. Bulk Create Customers

**POST**

```text
/api/customers/bulk
```

Full URL:

```text
http://localhost:8080/api/customers/bulk
```

Request:

```json
[
  {
    "name": "Aarav Sharma",
    "email": "aarav.sharma@example.com",
    "age": 31,
    "phoneNumber": "9876500001",
    "address": "Bengaluru, Karnataka",
    "status": "ACTIVE",
    "customerType": "REGULAR"
  },
  {
    "name": "Priya Nair",
    "email": "priya.nair@example.com",
    "age": 29,
    "phoneNumber": "9876500002",
    "address": "Chennai, Tamil Nadu",
    "status": "ACTIVE",
    "customerType": "PREMIUM"
  }
]
```

---

# CRUD Summary

| HTTP Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/customers` | Create customer |
| GET | `/api/customers` | Get all customers |
| GET | `/api/customers/{id}` | Get customer by ID |
| PUT | `/api/customers/{id}` | Update customer |
| DELETE | `/api/customers/{id}` | Delete customer |
| POST | `/api/customers/bulk` | Create multiple customers |

---

# PostgreSQL Useful Commands

Connect:

```powershell
psql -U postgres -d customer_db -h localhost
```

List tables:

```sql
\dt
```

Describe customer table:

```sql
\d customer
```

Detailed table information:

```sql
\d+ customer
```

View all customers:

```sql
SELECT * FROM customer ORDER BY id;
```

Count customers:

```sql
SELECT COUNT(*) FROM customer;
```

View selected customer fields:

```sql
SELECT id, name, email, age, phone_number, address, status, customer_type
FROM customer
ORDER BY id;
```

---

# Entity-to-Database Mapping

Java field names and PostgreSQL column names do not have to be identical.

Recommended convention:

```text
Java                    PostgreSQL
-----------------------------------------
customerType        ->  customer_type
phoneNumber         ->  phone_number
createdAt           ->  created_at
updatedAt           ->  updated_at
```

For explicit mappings, use:

```java
@Column(name = "customer_type")
private String customerType;

@Column(name = "phone_number")
private String phoneNumber;
```

Required import:

```java
import jakarta.persistence.Column;
```

Java is case-sensitive. PostgreSQL unquoted identifiers are normally folded to lowercase, so explicit `@Column` mappings are useful when names differ.

---

# Flyway Database Migrations

Database schema changes are managed using Flyway.

Migration files are located under:

```text
src/main/resources/db/migration/
```

Current migrations:

```text
V2__add_customer_status.sql
V3__add_customer_type.sql
```

Flyway migration naming convention:

```text
V<version>__<description>.sql
```

Example:

```text
V4__add_created_at.sql
```

Example migration:

```sql
ALTER TABLE customer
ADD COLUMN created_at TIMESTAMP;
```

## Flyway History

Flyway stores migration execution history in:

```text
flyway_schema_history
```

Query migration history:

```sql
SELECT
    installed_rank,
    version,
    description,
    script,
    success,
    installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Flyway Rules

Once a migration has been applied, do not modify the existing migration file.

For a new database change, create a new migration.

Example:

```text
V2__add_customer_status.sql
V3__add_customer_type.sql
V4__add_created_at.sql
V5__add_updated_at.sql
```

Think of applied migrations as immutable history.

---

# Hibernate Configuration

Hibernate is configured to validate the database schema:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Flyway is responsible for changing the database schema.

Therefore:

```text
Flyway
  -> Schema changes

Hibernate/JPA
  -> Entity-to-table mapping
  -> Schema validation
```

This is preferred for production-style schema management over:

```properties
spring.jpa.hibernate.ddl-auto=update
```

---

# Project Structure

```text
customer-api
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.example.customerapi
│   │   │       │
│   │   │       ├── CustomerApiApplication.java
│   │   │       │
│   │   │       ├── controller
│   │   │       │   └── CustomerController.java
│   │   │       │
│   │   │       ├── entity
│   │   │       │   └── Customer.java
│   │   │       │
│   │   │       ├── repository
│   │   │       │   └── CustomerRepository.java
│   │   │       │
│   │   │       └── service
│   │   │           └── CustomerService.java
│   │   │
│   │   └── resources
│   │       ├── application.properties
│   │       └── db
│   │           └── migration
│   │               ├── V2__add_customer_status.sql
│   │               └── V3__add_customer_type.sql
│   │
│   └── test
│
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

# Postman Testing

Use the Postman Desktop Agent for local APIs such as:

```text
http://localhost:8080
```

### Create customer

```text
POST http://localhost:8080/api/customers
```

Body:

```json
{
  "name": "Anil Kumar",
  "email": "anil.kumar@example.com",
  "age": 35,
  "phoneNumber": "9876543210",
  "address": "Bengaluru, Karnataka",
  "status": "ACTIVE",
  "customerType": "PREMIUM"
}
```

### Get all

```text
GET http://localhost:8080/api/customers
```

### Get one

```text
GET http://localhost:8080/api/customers/1
```

### Update

```text
PUT http://localhost:8080/api/customers/1
```

### Delete

```text
DELETE http://localhost:8080/api/customers/1
```

---

# Git Commands

Initialize the repository:

```powershell
git init
```

Check status:

```powershell
git status
```

Add files:

```powershell
git add .
```

Commit:

```powershell
git commit -m "Implement customer CRUD APIs"
```

Add remote:

```powershell
git remote add origin https://github.com/<username>/customer-api.git
```

Check remote:

```powershell
git remote -v
```

Push to the `master` branch:

```powershell
git push -u origin master
```

---

# Security

Never commit:

- Database passwords
- API keys
- Access tokens
- Private keys
- Certificates containing secrets

The PostgreSQL password is supplied through:

```text
DB_POSTGRES_PASSWORD
```

rather than being stored directly in `application.properties`.

Example:

```powershell
$env:DB_POSTGRES_PASSWORD="YOUR_POSTGRES_PASSWORD"
```

The repository should contain:

```properties
spring.datasource.password=${DB_POSTGRES_PASSWORD}
```

not the actual password.

---

# Development Milestones

This project has been developed in the following stages:

1. Java 21 and Maven setup
2. PostgreSQL 18.6 setup
3. Spring Boot project creation
4. PostgreSQL database creation
5. Customer entity
6. Spring Data JPA repository
7. Service layer
8. REST controller
9. Create customer API
10. Bulk customer API
11. Read all customers
12. Read customer by ID
13. Update customer
14. Delete customer
15. Flyway integration
16. Flyway baseline for existing schema
17. Versioned schema migrations
18. Added `status`
19. Added `customer_type`
20. Database password moved to environment variable

---

# Future Improvements

Planned improvements include:

- DTOs for request and response models
- Bean Validation
- Global exception handling
- Proper HTTP status codes
- Swagger / OpenAPI documentation
- Pagination and sorting
- Filtering and search
- Unit tests
- Integration tests
- Transaction management
- Docker
- CI/CD
- Authentication and authorization
- Production-ready configuration
- Environment-specific profiles
- Centralized secret management
