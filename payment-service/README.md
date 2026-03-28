# Payment Service

The **Payment Service** handles financial transactions and integrates with external payment providers (Razorpay).

## Responsibilities
- Initializing payment intents for orders.
- Verifying client-side payment success.
- Handling Razorpay webhooks.

## Port
- `8084`

## API Endpoints
- `POST /payments/initiate/{orderId}`: Generate a Razorpay order ID for an existing system order.
- `POST /payments/verify`: Verify the Razorpay signature after client-side payment completion.
- `POST /payments/webhook`: Entry point for external Razorpay event notifications.
- `GET /payments/{id}`: Retrieve internal payment record status.

## Event Driven Behavior
- **Consumes:** `OrderCreatedEvent` from Kafka.
- **Produces:** `PaymentEvent` (Success/Fail) to Kafka once processing is complete.

## Tech Details
- **Database:** `payment_db`
- **Integration:** Razorpay SDK.
