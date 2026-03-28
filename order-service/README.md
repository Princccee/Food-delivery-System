# Order Service

The **Order Service** is the engine of the application, managing the lifecycle of customer orders.

## Responsibilities
- Handling order placement (validating items with `restaurant-service`).
- Coordinating with `payment-service` via Kafka.
- Tracking order and payment status updates.

## Port
- `8083`

## API Endpoints
- `POST /api/orders`: Place a new order (Requires JWT).
- `GET /api/orders/{id}`: Get order status and details.
- `GET /api/orders/customer/{customerId}`: Get order history for a specific customer.
- `GET /api/orders/restaurant/{restaurantId}`: Get all orders placed at a specific restaurant.
- `POST /api/orders/{orderId}/payment-callback`: webhook for manual payment status overrides.

## Event Driven Behavior
- **Produces:** `OrderCreatedEvent` when a new order is saved.
- **Consumes:** `PaymentEvent` to transition order status from `CREATED` to `CONFIRMED` or `CANCELLED`.

## Tech Details
- **Database:** `order_db`
- **Communication:** `RestTemplate` (sync), Kafka (async).
