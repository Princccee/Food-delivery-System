# Food Delivery System - Architecture Report

This report provides a comprehensive overview of the microservices-based backend for the Food Delivery System.

## System Architecture

The system follows a **Microservices Architecture** with a **Database-per-Service** pattern. It combines synchronous RESTful communication for direct operations and asynchronous event-driven messaging via Apache Kafka for decoupling long-running processes like payments and notifications.

### 1. Nginx Gateway (Entry Point)
- **Port:** 8080
- **Purpose:** Acts as a reverse proxy and unified entry point for the frontend. It handles routing and CORS (Cross-Origin Resource Sharing) configuration, ensuring the frontend can securely communicate with all backend services.

### 2. Core Microservices

#### **Auth Service (8081)**
- **Database:** `auth_db`
- **Responsibility:** Manages user identity, registration, and authentication. It issues JWT (JSON Web Tokens) that are used across the system for security.

#### **Restaurant Service (8082)**
- **Database:** `restaurant_db`
- **Responsibility:** Manages restaurant profiles, status (open/closed), and menu item details (prices, availability).

#### **Order Service (8083)**
- **Database:** `order_db`
- **Responsibility:** The core domain service. It manages the lifecycle of an order from placement to completion. It coordinates with the Restaurant service for validation and triggers the payment flow.

#### **Payment Service (8084)**
- **Database:** `payment_db`
- **Responsibility:** Handles financial transactions, integrating with external providers like Razorpay. It processes payments asynchronously to ensure the user experience remains responsive.

#### **Notification Service (8085)**
- **Database:** `notification_db`
- **Responsibility:** Listens for system-wide events (like order confirmation or payment success) to send real-time updates and logs to users.

---

## Internal Data Flow (Order Lifecycle)

The system utilizes a combination of REST and Kafka to manage complex workflows:

1.  **Order Initiation (REST):**
    - The client sends a request to `/api/orders`.
    - **Order Service** calls **Restaurant Service** via synchronous REST to fetch the latest menu prices and verify availability.
2.  **Order Creation (Asynchronous):**
    - **Order Service** saves the order in `PENDING` status.
    - An `OrderCreatedEvent` is published to the **Kafka** `order-created-topic`.
3.  **Payment Processing:**
    - **Payment Service** consumes the `OrderCreatedEvent`.
    - It processes the transaction and saves the result in `payment_db`.
    - A `PaymentStatusEvent` is published back to **Kafka**.
4.  **Order Fulfillment:**
    - **Order Service** consumes the `PaymentStatusEvent` and updates the order status to `PAID` or `FAILED` accordingly.
    - **Notification Service** consumes these events to alert the user of their order status.

---

## Infrastructure
- **MySQL 8.0:** Distributed databases ensured isolation and scalability.
- **Apache Kafka & Zookeeper:** Provided the backbone for reliable, event-driven communication.
- **Docker Compose:** Orchestrates the entire ecosystem for reproducible deployments.

![System Architecture](/Users/prince/.gemini/antigravity/brain/abd73c5c-22b9-4188-9e27-1039a7d74cd5/system_architecture_diagram_1774775093504.png)
