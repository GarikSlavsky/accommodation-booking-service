# Accommodation Booking Service

## Introduction

Welcome to the <span style="font-size: 16px;">Accommodation Booking Service</span>, a web application designed to modernize and streamline the housing rental experience. 
This project replaces outdated, manual processes—such as physical paperwork, cash-only payments, and lack of real-time availability—with an advanced online platform. 
It empowers administrators to manage accommodations efficiently and provides renters with a seamless, user-friendly booking experience. 
Built with Spring Boot, this project showcases expertise in backend development, secure authentication, and scalable database operations.

## Features

- <span style="font-size: 14px;"><b>Accommodation Management:</b></span> Add, update, delete, and retrieve accommodations (e.g., houses, apartments) with real-time availability tracking.

- <span style="font-size: 14px;"><b>User Management:</b></span> Secure user registration, authentication, and profile management with role-based access (Manager and Customer).

- <span style="font-size: 14px;"><b>Booking Management:</b></span> Create, update, cancel, and view bookings with status tracking (Pending, Confirmed, Canceled, Expired).

- <span style="font-size: 14px;"><b>Payment Processing:</b></span> Secure payment sessions via Stripe, including success and cancellation handling.

- <span style="font-size: 14px;"><b>Real-Time Availability:</b></span> Efficiently checks accommodation availability, preventing overbooking.

- <span style="font-size: 14px;"><b>Secure Authentication:</b></span> JWT-based authentication and role-based authorization using Spring Security.

- <span style="font-size: 14px;"><b>API Documentation:</b></span> Interactive Swagger UI for exploring and testing API endpoints.

- <span style="font-size: 14px;"><b>Data Validation:</b></span> Robust input validation with custom annotations (e.g., @FieldMatch for password confirmation).

- <span style="font-size: 14px;"><b>Secure Configuration:</b></span> Sensitive data (e.g., database credentials, JWT secrets, Stripe keys) stored in a `.env` file.

- <span style="font-size: 14px;"><b>Persistence:</b></span> Reliable data storage using Spring Data JPA with PostgreSQL.

## Technologies Used

<span style="font-size: 14px;"><b>The application leverages the following technologies and tools:</b></span>

- <span style="font-size: 14px;"><b>Spring Boot (3.4.1):</b></span> Framework for building the backend application.

- <span style="font-size: 14px;"><b>Spring Security (6.4.2):</b></span> Provides JWT-based authentication and role-based access control.

- <span style="font-size: 14px;"><b>Spring Data JPA (3.4.1):</b></span> Manages database operations with PostgreSQL.

- <span style="font-size: 14px;"><b>Hibernate Validator (8.x):</b></span> Validates input data with custom constraints (e.g., `@FieldMatch`).

- <span style="font-size: 14px;"><b>Stripe API:</b></span> Integrates secure payment processing.

- <span style="font-size: 14px;"><b>Swagger/OpenAPI (2.8.4):</b></span> Generates interactive API documentation with `@Operation` and `@Tag`.

- <span style="font-size: 14px;"><b>Lombok (1.18.36):</b></span> Reduces boilerplate code (e.g., getters, setters).

- <span style="font-size: 14px;"><b>PostgreSQL (15.x):</b></span> Relational database for persistent storage.

- <span style="font-size: 14px;"><b>java-dotenv (5.2.2):</b></span> Loads sensitive configuration from `.env` files.

- <span style="font-size: 14px;"><b>Maven:</b></span> Dependency management and build tool.

- <span style="font-size: 14px;"><b>JUnit (5.8.2):</b></span> Supports unit and integration testing.

- <span style="font-size: 14px;"><b>Mockito (5.14.2):</b></span> Mocking framework for testing.

- <span style="font-size: 14px;"><b>Docker (v28.0.1):</b></span> Containerization for deployment (if implemented).

- <span style="font-size: 14px;"><b>Postman:</b></span> Tool for manual API testing.

## Architecture Overview

<span style="font-size: 14px;"><b>The application follows a modular, layered architecture to ensure scalability and maintainability:</b></span>

- <span style="font-size: 14px;"><b>Controllers:</b></span> Handle HTTP requests and delegate to services (e.g., `AccommodationController`, `BookingController`).

- <span style="font-size: 14px;"><b>Services:</b></span> Encapsulate business logic and coordinate with repositories (e.g., `AccommodationService`, `PaymentService`).

- <span style="font-size: 14px;"><b>Repositories:</b></span> Interact with the PostgreSQL database using Spring Data JPA.

- <span style="font-size: 14px;"><b>Entities:</b></span> Represent data models (e.g., `Accommodation`, `User`, `Booking`, `Payment`).

- <span style="font-size: 14px;"><b>DTOs:</b></span> Transfer data between layers, validated with annotations (e.g., `@NotBlank`, `@FieldMatch`).

- <span style="font-size: 14px;"><b>Validation:</b></span> Custom annotations (e.g., `@FieldMatch`) ensure data integrity (e.g., matching passwords).

## Database Schema

<span style="font-size: 14px;"><b>The schema includes the following entities:</b></span>

***Accommodation:***

- `ID`: Long (Primary key)

- `Type`: Enum (HOUSE, APARTMENT, CONDO, VACATION_HOME)

- `Location`: String (Address)

- `Size`: String (e.g., Studio, 1 Bedroom)

- `Amenities`: Array of Strings

- `Daily Rate`: BigDecimal (Price per day in USD)

- `Availability`: Integer (Number of available units)

***User:***

- `ID`: Long (Primary key)

- `Email`: String

- `First Name`: String

- `Last Name`: String

- `Password`: String (Securely hashed)

- `Role`: Enum (MANAGER, CUSTOMER)

***Role:***

- `ID`: Long (Primary key)

- `Name`: String

- `RoleName`: Enum (MANAGER, CUSTOMER)

***Booking:***

- `ID`: Long (Primary key)

- `Check-in Date`: LocalDate

- `Check-out Date`: LocalDate

- `Accommodation ID`: Long (Foreign key)

- `User ID`: Long (Foreign key)

- `Status`: Enum (PENDING, CONFIRMED, CANCELED, EXPIRED)

***Payment:***

- `ID`: Long (Primary key)

- `Status`: Enum (PENDING, PAID)

- `Booking ID`: Long (Foreign key)

- `Session URL`: String (Stripe payment URL)

- `Session ID`: String (Stripe session ID)

- `Amount to Pay`: BigDecimal (Total in USD)

## Controllers

<span style="font-size: 14px;"><b>The application exposes RESTful endpoints across several controllers:</b></span>

1. ### User Management (`/auth`, `/users`):

- `POST /auth/register`: Register a new user with validated email and password.

- `POST /auth/login`: Authenticate a user and return a JWT token.

- `PATCH /users/me/profile`: Update user profile (name, email).

- `PATCH /users/me/password`: Update user password.

- `GET /users/me`: Retrieve current user’s profile.

- `PUT /users/{id}/role` (Manager only): Update a user’s role.

2. ### Accommodation Management (`/accommodations`):

- `POST /accommodations` (Manager only): Add a new accommodation.

- `GET /accommodations/{id}`: Retrieve accommodation details by ID.

- `GET /accommodations`: List available accommodations (paginated).

- `PUT /accommodations/{id}` (Manager only): Update accommodation details.

- `DELETE /accommodations/{id}` (Manager only): Soft-delete an accommodation.

3. ### Booking Management (`/bookings`):

- `POST /bookings` (Authenticated): Create a new booking.

- `GET /bookings` (Manager only): Retrieve bookings by user ID or status (paginated).

- `GET /bookings/my` (Authenticated): Retrieve current user’s bookings.

- `GET /bookings/{id}` (Authenticated): Retrieve booking details by ID.

- `PUT /bookings/{id}` (Authenticated): Update booking details.

- `PATCH /bookings/{id}` (Manager only): Update booking status.

- `DELETE /bookings/{id}` (Authenticated): Cancel a booking.

4. ### Payment Management (`/payments`)

- `GET /payments` (Authenticated): Retrieve payments for a user or all (Manager).

- `POST /payments` (Authenticated): Initiate a Stripe payment session.

- `POST /payments/renew/{paymentId}` (Authenticated): Renew an expired payment session.

- `GET /payments/success`: Handle successful Stripe payments.

- `GET /payments/cancel`: Handle canceled Stripe payments.

## Postman documentation

For detailed API testing instructions, visit the [Postman Collection](https://web.postman.co/workspace/e2a3c8ec-9568-4c61-8556-8c61dabec80f/documentation/41098914-c567dbd5-518a-4e56-a9a7-74888b78e426).

## Model Diagram

Below is the **model diagram**, which illustrates the structure of entities and their relationships in the application:

![Model Diagram](docs/schema.JPG)

## Prerequisites

- <span style="font-size: 14px;"><b>Java:</b></span> 17 or higher

- <span style="font-size: 14px;"><b>Maven:</b></span> 3.8.x or higher

- <span style="font-size: 14px;"><b>PostgreSQL:</b></span> 15.x or higher

- <span style="font-size: 14px;"><b>Stripe Account:</b></span> For payment processing

- <span style="font-size: 14px;"><b>Docker (optional):</b></span> For containerized deployment

## Setup Instructions

### 1. Clone the Repository

```
git clone https://github.com/your-username/accommodation-booking-service.git
cd accommodation-booking-service
```

### 2. Configure Environment Variables

Copy the `.env.template` to `.env` and fill in the required values:

```
cp .env.template .env
```

`.env.template`

```aiignore
# Database configuration
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# JWT configuration
JWT_SECRET=
JWT_EXPIRATION=

# Stripe configuration
STRIPE_SECRET_KEY=

# Telegram configuration (optional, for notifications)
TELEGRAM_BOT_TOKEN=
TELEGRAM_CHAT_ID=
```

Edit `.env` with your values:

```aiignore
DB_URL=jdbc:postgresql://localhost:5432/accommodation_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=1234567890
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_CHAT_ID=your_telegram_chat_id
```

### 3. Set Up PostgreSQL

- Install PostgreSQL and create a database named `accommodation_db`.

- Update the database credentials in `.env` if necessary.

- The schema is managed automatically by Spring Data JPA (or Liquibase, if configured).

### 4. Build and Run the Application

```aiignore
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### 5. Access Swagger UI

Explore and test API endpoints via Swagger:

```aiignore
http://localhost:8080/swagger-ui.html
```

## Running with Docker 

- Build the Docker image:

```aiignore
docker build -t accommodation-booking-service .
```

- Run the container, passing environment variables:

```aiignore
docker run -p 8080:8080 --env-file .env accommodation-booking-service
```
