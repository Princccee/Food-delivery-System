# Food Delivery System - Microservices Backend

This repository contains a Spring Boot-based microservices architecture for a comprehensive food delivery platform.

## Architecture Overview

The system is designed with a **Database-per-Service** pattern and utilizes both synchronous (REST) and asynchronous (Kafka) communication between services.

### Components

1.  **API Gateway** (`:8080`): Single entry point that routes requests to appropriate services.
2.  **Auth Service** (`:8081`): Handles user registration, login, and JWT-based authentication.
3.  **Restaurant Service** (`:8082`): Manages restaurant profiles and their menu items.
4.  **Order Service** (`:8083`): Core domain service for placing and tracking orders.
5.  **Payment Service** (`:8084`): Integration with Razorpay for transaction processing.
6.  **Notification Service** (`:8085`): Skeleton for event-driven notifications.

### Technology Stack
- **Backend:** Java 17, Spring Boot 3.2+
- **Build Tool:** Gradle
- **Messaging:** Apache Kafka
- **Databases:** MySQL 8.0
- **Security:** Spring Security + JWT
- **Deployment:** Docker & Docker Compose

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17+ (optional, if building without Docker)

### Run with Docker Compose
To spin up the entire ecosystem (all 6 services + Kafka + MySQL) with a single command:

```bash
docker-compose up -d --build
```

- **Kafka UI:** Access [http://localhost:8090](http://localhost:8090) to monitor event topics.
- **MySQL:** Exposed on `localhost:3307` (User: `fooduser`, Pass: `Food@12345`).

### Local Development
Each service is a standalone Gradle project. You can run them individually by setting up a local MySQL instance and Kafka broker, then running:
```bash
./gradlew bootRun
```

---

## Inter-Service Flow
1.  **Order Placement:** Client hits `api-gateway` ➔ `order-service`.
2.  **Validation:** `order-service` calls `restaurant-service` (REST) to fetch menu prices.
3.  **Event:** `order-service` saves order and publishes `OrderCreatedEvent` to Kafka.
4.  **Payment:** `payment-service` consumes the event and initiates a transaction.
5.  **Update:** Upon payment success/failure, `payment-service` publishes a `PaymentEvent` which `order-service` consumes to finalize the order.
