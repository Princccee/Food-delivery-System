# Restaurant Service

The **Restaurant Service** is responsible for managing restaurant profiles and their associated menu catalogs.

## Responsibilities
- Onboarding restaurants.
- Managing menu items (names, prices, descriptions).
- Serving menu data to the `order-service`.

## Port
- `8082`

## API Endpoints

### Restaurants
- `POST /restaurants`: Create a new restaurant profile.
- `GET /restaurants`: List all available restaurants.
- `GET /restaurants/{id}`: Get specific restaurant details.

### Menu Items
- `POST /menu-items/{restaurantId}`: Add a new item to a restaurant's menu.
- `GET /menu-items/{restaurantId}`: Get the full menu for a restaurant.
- `GET /menu-items/internal/{restaurantId}`: (Internal use) Highly optimized menu retrieval for order validation.

## Tech Details
- **Database:** `restaurant_db`
