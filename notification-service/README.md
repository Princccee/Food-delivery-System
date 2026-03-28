# Notification Service

The **Notification Service** is a dedicated service for sending customer communications (Email, SMS, Push).

## Status: Skeleton
This service currently exists as a baseline for future extension. It includes standard Spring Boot Health markers and Kafka/AMQP dependencies.

## Responsibilities (Planned)
- Consume `OrderCreatedEvent` to send confirmation emails.
- Consume `PaymentEvent` to send receipts.
- Consumes `DeliveryEvent` (future) to send status updates.

## Port
- `8085`

## Tech Details
- **Database:** `notification_db`
