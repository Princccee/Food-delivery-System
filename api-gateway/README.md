# API Gateway Service

The **API Gateway** acts as the central entry point for the Food Delivery System. It provides a unified URL space and handles routing to downstream microservices using **Spring Cloud Gateway**.

## Responsibilities
- Centralized request routing based on path predicates.
- Decoupling clients from the internal microservice network.
- *Future:* Cross-cutting concerns like rate limiting and logging.

## Port
- `8080`

## Routing Rules
| Path | Service |
| :--- | :--- |
| `/auth/**`, `/users/**` | `auth-service` |
| `/restaurants/**`, `/menu-items/**` | `restaurant-service` |
| `/api/orders/**` | `order-service` |
| `/payments/**` | `payment-service` |

---

## Running Locally
```bash
./gradlew bootRun
```
Requires the other microservices to be running for successful routing.
