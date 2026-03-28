# Auth Service

The **Auth Service** manages user accounts and provides secure authentication using JSON Web Tokens (JWT).

## Responsibilities
- User registration and password hashing.
- User authentication and JWT issuance.
- User profile retrieval.

## Port
- `8081`

## API Endpoints

### Authentication
- `POST /auth/register`: Register a new user (`username`, `password`, `email`).
- `POST /auth/login`: Authenticate and receive a JWT.

### User
- `GET /users/me`: Retrieve current authenticated user details (requires JWT).

## Tech Details
- **Database:** `auth_db`
- **Security:** Spring Security + `jjwt`
