# E-Commerce Backend (Spring) — README

> Backend for an e-commerce platform with authentication, three roles (USER, SELLER, ADMIN), and modules for Category, Product, Cart, Order, Wishlist, Payment (Stripe), and Seller onboarding.
> Built with Spring (4.x), Spring Security (6+), Maven and PostgreSQL.

This README is a **ready-to-use guide** for running, configuring, testing and extending the project. It includes configuration notes, required environment variables, development & production tips, security notes, important endpoint examples (Postman / cURL), and recommended next steps.

---

# Table of contents

* [Project overview](#project-overview)
* [Features](#features)
* [Architecture & packages](#architecture--packages)
* [Tech stack & requirements](#tech-stack--requirements)
* [Environment configuration](#environment-configuration)
* [Local setup](#local-setup)
* [Running the app](#running-the-app)
* [Database & migrations](#database--migrations)
* [Important environment properties](#important-environment-properties)
* [Auth flow & tokens (how it works)](#auth-flow--tokens-how-it-works)
* [Key endpoints (examples & Postman-ready)](#key-endpoints-examples--postman-ready)
* [Seller onboarding flow](#seller-onboarding-flow)
* [Payment (Stripe) flow summary](#payment-stripe-flow-summary)
* [Exception handling & responses](#exception-handling--responses)
* [Testing & Postman collection suggestions](#testing--postman-collection-suggestions)
* [Security & deployment notes](#security--deployment-notes)
* [Developer notes & recommended next steps](#developer-notes--recommended-next-steps)
* [Background Jobs](#background-jobs)
* [Contributing / Code style / Commit message convention](#contributing--code-style--commit-message-convention)
* [License](#license)

---

# Project overview

This service implements the backend for an e-commerce application. It provides:

* Authentication and authorization (JWT) with refresh token flow
* Three roles: `ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`
* Product and category management
* Seller onboarding (user requests seller role, admin approves/rejects)
* Cart & Wishlist functionality
* Order creation (from cart or direct), cancellation and retrieval
* Payment integration using Stripe (payment session & webhook handling)
* Cloudinary integration for file uploads (profile images, product images, KYC docs)
* Global exception handling with well-structured JSON error responses

---

# Features

* Register / Verify email / Login / Refresh token / Logout
* Update profile, change email (verify flow), reset password via OTP
* Role-based access control (RBAC) with `@PreAuthorize`
* Seller onboarding request + admin review workflow
* Product CRUD (seller owns product; admin can override)
* Shopping cart management (add/update/remove/clear)
* Wishlist management
* Order creation and cancellation
* Stripe payment integration (create session, webhook, refund handling)
* Cloudinary file uploads
* Global exception handler with consistent error shape

---

# Architecture & packages

High-level package layout (suggested by project):

```
com.example.backend
├── auth                # authentication controllers, DTOs, service (AuthServiceImpl)
├── Category            # category service/controller/entity
├── Product             # product service/controller/entity
├── Cart                # cart service/controller/entity
├── Order               # order service/controller/entity
├── Wishlist            # wishlist service/controller/entity
├── payment             # stripe payment related code
├── scheduler           # background jobs like OTP cleanup 
├── seller              # seller onboarding (SellerRequest, SellerProfile, service + controller)
├── repository          # shared repos (UsersRepo etc.)
├── exception           # global exceptions & handler
└── util                # email service, etc.
```

---

# Tech stack & requirements

* Java 17+ (or version compatible with Spring Boot 3/4 — match your Spring version)
* Spring Boot 4.x
* Spring Security 6.x
* Maven (build & dependency management)
* PostgreSQL (production DB)
* Cloudinary (file uploads)
* Stripe (payments)
* Gmail SMTP (email notifications)
* Optional: Redis (for token blacklisting, if later needed)
* Optional: Flyway or Liquibase for migrations

---

# Environment configuration

**Do not commit real secrets.** Use `application-example.properties` as a template, and add `application.properties` to `.gitignore`.

Create `src/main/resources/application-example.properties` (already provided in project). Copy it to `src/main/resources/application.properties` and fill values:

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
# edit the file and fill database, jwt, cloudinary, stripe, mail credentials
```

Important variables (placeholders shown in example file):

* `spring.datasource.url` / `username` / `password`
* `jwt.secret`
* `spring.mail.username` / `spring.mail.password`
* `cloudinary.cloud_name`, `cloudinary.api_key`, `cloudinary.api_secret`
* `stripe.secretKey`
* `app.base-url` (useful for verification links)
* `payment.session.ttl.minutes` and cron for expiry checks

---

# Local setup

1. Clone the repo:

   ```bash
   git clone https://github.com/Faresaymann/ecommerce-platform.git
   cd ecommerce-platform
   ```

2. Create local config:

   ```bash
   cp src/main/resources/application-example.properties src/main/resources/application.properties
   # edit application.properties with your values
   ```

3. Ensure PostgreSQL is running and database exists:

   ```sql
   CREATE DATABASE e_commerce_db;
   CREATE USER your_db_user WITH PASSWORD 'your_db_password';
   GRANT ALL PRIVILEGES ON DATABASE e_commerce_db TO your_db_user;
   ```

4. Build:

   ```bash
   mvn clean package
   ```

5. Run:

   ```bash
   mvn spring-boot:run
   # or
   java -jar target/backend-0.0.1-SNAPSHOT.jar
   ```

---

# Running the app

* Default port: `8080` (or `server.port` if configured)
* Base API: `http://localhost:8080/api/v1`

---

# Database & migrations

* Currently `spring.jpa.hibernate.ddl-auto=update` (development convenience).
* For production, switch to `validate` and use Flyway or Liquibase for schema migrations.
* If you remove columns (e.g., `payout_account`, `store_description`, `vat_number`) use a migration script.

---

# Important environment properties (high-level)

Example placeholders (already in `application-example.properties`):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/E_Commerence_DB
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

jwt.secret=your_jwt_secret_here

spring.mail.username=your_email@gmail.com
spring.mail.password=your_email_password

cloudinary.cloud_name=your_cloud_name
cloudinary.api_key=your_cloudinary_api_key
cloudinary.api_secret=your_cloudinary_api_secret

stripe.secretKey=your_stripe_secret_key
app.base-url=http://localhost:8080
```

---

# Auth flow & tokens (how it works)

* **Login** returns:

  * `accessToken` (short-lived JWT, e.g., 5–15 minutes)
  * `refreshToken` (longer-lived JWT or opaque token)

* **Refresh** (`POST /api/v1/auth/refresh-token`)
  Request body:

  ```json
  { "token": "<refresh_token>" }
  ```

  Response: new access token (refresh token reused unless you implement rotation).

* **Logout** is stateless: client deletes tokens. (If you later store refresh tokens in DB, the logout endpoint can revoke them.)

* Controllers get authenticated user email via:

  ```java
  String email = authentication.getName();
  ```

* Role checks use `@PreAuthorize("hasRole('ADMIN')")`, etc.

---

# Key endpoints (examples & Postman-ready)

Below are the most commonly used endpoints with example requests. Replace `<ACCESS_TOKEN>`, `<REFRESH_TOKEN>`, `<ADMIN_TOKEN>`, `<SELLER_TOKEN>`.

## Auth

* Register (multipart/form-data):

  ```
  POST /api/v1/auth/register
  Content-Type: multipart/form-data
  Params: firstName, lastName, email, password, role (ROLE_USER/ROLE_SELLER optional), file (optional)
  ```
* Verify email:

  ```
  GET /api/v1/auth/verify-email?token=<token>
  ```
* Login:

  ```
  POST /api/v1/auth/login
  Body: { "email":"user@example.com", "password":"pass" }
  ```
* Refresh:

  ```
  POST /api/v1/auth/refresh-token
  Body: { "token":"<refresh_token>" }
  ```
* Logout:

  ```
  POST /api/v1/auth/logout
  Header: Authorization: Bearer <ACCESS_TOKEN>
  Body (optional): { "refreshToken": "<refresh_token>" }
  ```

## Seller onboarding

* Request seller (authenticated user):

  ```
  POST /api/v1/seller/request (multipart/form-data)
  Header: Authorization: Bearer <ACCESS_TOKEN>
  Params: storeName, reason (optional), document (file)
  ```
* Admin list pending:

  ```
  GET /api/v1/seller/seller-requests
  Header: Authorization: Bearer <ADMIN_TOKEN>
  ```
* Approve request:

  ```
  POST /api/v1/seller/approve/{requestId}
  Header: Authorization: Bearer <ADMIN_TOKEN>
  ```
* Reject request:

  ```
  POST /api/v1/seller/reject/{requestId}?reason=invalid_docs
  Header: Authorization: Bearer <ADMIN_TOKEN>
  ```

## Product (seller)

* Create product:

  ```
  POST /api/v1/products (multipart/form-data)
  Header: Authorization: Bearer <SELLER_TOKEN>
  Params: name, description, price, categoryId, stock, file (optional)
  ```
* Update product:

  ```
  PUT /api/v1/products/{id} (multipart/form-data)
  Header: Authorization: Bearer <SELLER_TOKEN> or ADMIN
  ```
* Delete:

  ```
  DELETE /api/v1/products/{id}
  Header: Authorization: Bearer <SELLER_TOKEN> or ADMIN
  ```

## Cart

* Add:

  ```
  POST /api/v1/cart/add
  Header: Authorization: Bearer <ACCESS_TOKEN>
  Body: { "productId":"<uuid>", "quantity":1 }
  ```
* Update, Remove, Get, Clear — as in controllers.

## Wishlist

* Add:

  ```
  POST /api/v1/wishlist/add/{productId}
  Header: Authorization: Bearer <ACCESS_TOKEN>
  ```

## Orders

* Create from cart:

  ```
  POST /api/v1/orders/from-cart
  Header: Authorization: Bearer <ACCESS_TOKEN>
  Body: { "shippingAddress":"..." }
  ```
* Create direct:

  ```
  POST /api/v1/orders/direct
  Body: { "productId":"<uuid>", "quantity":1, "shippingAddress":"..." }
  ```
* Cancel:

  ```
  POST /api/v1/orders/{orderId}/cancel
  Header: Authorization: Bearer <ACCESS_TOKEN>
  ```

## Payment (Stripe)

* Start payment session: `POST /api/v1/payment/create-session`
* Webhook endpoint to handle events (configured in Stripe dashboard): `/api/v1/payment/webhook`

---

# Seller onboarding flow (summary)

1. User submits request (storeName, optional reason, KYC doc).
2. `SellerRequest` stored with `PENDING` status.
3. Admin lists pending requests and approves/rejects.

   * Approve: create or update `SellerProfile`, set user role to `ROLE_SELLER`, notify user.
   * Reject: store reason, notify user.
4. User logs in again to receive a token reflecting the new role (or you can implement token refresh).

---

# Payment (Stripe) flow summary

* Server creates a Stripe Session (Checkout or PaymentIntent).
* Client uses Stripe JS or mobile SDK to complete payment.
* Server receives Stripe webhook events (payment succeeded, failed).
* On success, server marks Order as `PAID` and decrements stock.
* Refunds handled via Stripe API and update local payment records.

**Important:** Keep `stripe.secretKey` secret. Do not commit to VCS.

---

# Exception handling & responses

* Global exception handler returns consistent JSON body:

  ```json
  {
    "status": 400,
    "timestamp": "2025-12-22T18:34:10",
    "message": "You already have a pending seller request",
    "error": "Seller Request Error"
  }
  ```
* You have specialized exceptions (e.g., `ProductNotFoundException`, `SellerRequestException`) and global handler maps them to proper HTTP codes.
* Keep Business logic exceptions descriptive and consistent.

---

# Testing & Postman collection suggestions

* Prepare a Postman collection (or export) with:

  * Auth flows: register, verify, login, refresh, logout
  * Seller request & admin approve/reject (with two environments: user/admin tokens)
  * Product CRUD (create product with file upload)
  * Cart & Order flows (create order from cart and direct)
  * Stripe session creation & webhook simulator
* Include environment variables:

  * `baseUrl`, `accessToken`, `refreshToken`, `adminToken`, `sellerToken`

---

# Security & deployment notes

* **Never** commit `application.properties` with secrets. Use `application-example.properties`.
* For production:

  * Use HTTPS (TLS)
  * Store secrets in environment variables or a secrets manager (Vault, AWS Secrets Manager)
  * Use database migrations (Flyway/Liquibase) instead of `ddl-auto=update`
  * Configure CORS properly and rate-limit sensitive endpoints (forgot-password)
  * Consider rotating refresh tokens and/or storing refresh tokens server-side with revocation
  * Consider using Redis for blacklisting tokens if you need server-side logout
* Use strong `jwt.secret` (256+ bit if using HS256) and rotate periodically
* Enforce secure cookies / SameSite policies if using cookies

---

# Developer notes & recommended next steps

* Add Swagger/OpenAPI annotations for automatic API docs (Springdoc or springfox).
* Add integration tests for:

  * Auth flow
  * Seller onboarding & admin approval
  * Product ownership checks
  * Cart → Order → Payment flow
* Add pagination & filters to product listing endpoints
* Add monitoring & alerting (Prometheus/Grafana)
* Consider moving file upload logic into a dedicated service to centralize Cloudinary usage

---
# Background Jobs

### OTP Cleanup Scheduler

The project includes a background scheduled job that automatically removes expired OTP (One-Time Password) records from the database.

* Implemented using Spring’s `@Scheduled`
* Runs periodically based on a configurable cron expression
* Prevents accumulation of expired OTPs and keeps the database clean

**Scheduler location:**

```
com.example.backend.scheduler.OTPCleanupScheduler
```

**Configuration (example):**

```
otp.cleaner.cron=0 0 * * * *  # runs every hour
```

---

### Payment Session Expiration

The system also includes a background job that checks for and expires unpaid payment sessions.

* Periodically scans for payment sessions that exceeded their TTL
* Marks expired sessions accordingly to prevent invalid or stale payments
* Schedule and TTL are configurable via application properties

**Configuration (example):**

```
payment.session.ttl.minutes=60
payment.expire-check-cron=0 */1 * * * *
```
  
---
# Contributing / Code style / Commit message convention

* Use Conventional Commits style, e.g.:

  * `feat(cart): add updateQuantity endpoint`
  * `docs(product): add Javadoc comments`
  * `fix(auth): validate refresh token`
* Keep commits focused (module or feature-sized).
* Follow existing package & naming conventions (camelCase for methods).
* Add tests for new behavior.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

# Quick start checklist (copy/paste)

1. `cp src/main/resources/application-example.properties src/main/resources/application.properties`
2. Fill in DB, cloudinary, mail, stripe keys
3. `mvn clean package`
4. `mvn spring-boot:run`
5. Use Postman to test `/api/v1/auth` endpoints; create a user and then test flows above
