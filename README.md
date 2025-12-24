# Customer Service

A Spring Boot REST API for managing customers and their addresses.
This project demonstrates clean REST design, validation, exception handling,
and comprehensive unit testing using real-world best practices.

---

## Tech Stack
- Java 17
- Spring Boot
- Spring MVC
- Spring Validation
- JUnit 5
- Mockito
- Maven

---

## API Endpoints

### Customer APIs
- POST `/api/customers` – Register a new customer
- GET `/api/customers/{id}` – Get customer details
- PUT `/api/customers/{id}` – Update customer profile
- PATCH `/api/customers/{id}` – Update customer email
- DELETE `/api/customers/{id}` – Deactivate (soft delete) customer

### Address APIs
- GET `/api/customers/{id}/addresses` – Get all addresses for a customer
- POST `/api/customers/{id}/addresses` – Add a new address
- PUT `/api/customers/{id}/addresses/{addressId}` – Update address
- DELETE `/api/customers/{id}/addresses/{addressId}` – Delete address

---

## Validation & Error Handling
- Uses `@Valid` for request validation
- Centralised exception handling via `@RestControllerAdvice`
- Returns proper HTTP status codes:
  - 400 – Bad Request (validation failures)
  - 404 – Resource Not Found
  - 409 – Conflict (duplicate email, inactive resources)

---

## Running the Application

### Prerequisites
- Java 17+
- Maven

### Steps
```bash
git clone https://github.com/manojkondamari/Customers.git
cd Customers
mvn spring-boot:run
