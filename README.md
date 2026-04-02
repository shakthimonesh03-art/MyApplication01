# Ticket Booking MVP (Phase 1)

This repository contains a **Phase 1 MVP** for a real-time event ticket booking platform.

## Implemented Phase 1 scope

- User signup/login (JWT)
- Event listing, event details, search by city/date/category
- Venue + seat layout creation
- Seat hold for a limited time (default 5 minutes)
- Booking creation from held seats
- Payment initiation + idempotent payment confirmation
- Booking confirmation and seat finalization
- Booking cancellation (if booking is still cancellable)
- Admin APIs for venue/event creation

---

## Prerequisites (local machine)

Install these first:

- Docker + Docker Compose plugin
- (Optional) Java 21 and Maven 3.9+ if you want to run without Docker

---

## Option A: Run with Docker (recommended)

### 1) Clone and open project

```bash
git clone <your-repo-url>
cd MyApplication01
```

### 2) Start all services

```bash
docker compose up --build
```

This starts:

- App API on `http://localhost:8080`
- PostgreSQL on `localhost:5432`
- Redis on `localhost:6379`

### 3) Stop services

```bash
docker compose down
```

### 4) Stop + remove DB volume (clean reset)

```bash
docker compose down -v
```

---

## Option B: Run app without Docker (DB in Docker)

### 1) Start only infra (Postgres + Redis)

```bash
docker compose up -d postgres redis
```

### 2) Run Spring Boot app locally

```bash
mvn clean spring-boot:run
```

App uses `src/main/resources/application.yml` defaults:

- DB URL: `jdbc:postgresql://localhost:5432/ticketdb`
- DB user: `ticket`
- DB password: `ticket`

---

## Default admin user

A bootstrap admin is created automatically at startup:

- Email: `admin@local.test`
- Password: `Admin@123`

Use this account to call:

- `POST /api/admin/venues`
- `POST /api/admin/events`

---

## Quick API smoke test (copy/paste)

### 1) Login as admin

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@local.test","password":"Admin@123"}'
```

Copy `token` from response.

### 2) Create venue

```bash
curl -s -X POST http://localhost:8080/api/admin/venues \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{"name":"PVR Koramangala","city":"Bengaluru","address":"Forum Mall","totalCapacity":200}'
```

### 3) Create event with seats

```bash
curl -s -X POST http://localhost:8080/api/admin/events \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{
    "title":"Rock Night",
    "description":"Live show",
    "category":"Music",
    "startTime":"2026-04-10T18:00:00Z",
    "endTime":"2026-04-10T21:00:00Z",
    "venueId":1,
    "basePrice":499,
    "seats":[
      {"rowLabel":"A","seatNumber":1,"category":"REGULAR","price":499},
      {"rowLabel":"A","seatNumber":2,"category":"REGULAR","price":499}
    ]
  }'
```

### 4) List events

```bash
curl -s http://localhost:8080/api/events
```

---

## Main APIs in MVP

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/events`
- `GET /api/events/search?city=...&category=...&fromDate=...`
- `GET /api/events/{eventId}/seats`
- `POST /api/inventory/hold`
- `POST /api/bookings`
- `POST /api/payments/initiate`
- `POST /api/payments/confirm`
- `POST /api/bookings/{id}/cancel`
- `GET /api/bookings/{id}/ticket`

---

## Notes on concurrency and idempotency

- Seat locking uses pessimistic row-level locking on selected seat rows and validates status + hold expiry before moving to `HELD`.
- Payment confirmation requires an idempotency key and enforces uniqueness in DB.

---

## Phase 2+ (next)

Coupons/offers, refunds, OpenSearch indexing, async notifications via Kafka, reporting dashboards, and real PDF/QR generation.
