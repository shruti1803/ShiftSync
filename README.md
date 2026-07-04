# ShiftSync — Internal Ops Portal

> 🚀 **Live API:** _Coming soon (Railway deployment)_
> 📖 **Swagger Docs:** _Coming soon_

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?style=flat-square&logo=redis)
![JWT](https://img.shields.io/badge/Auth-JWT%20%2B%20OAuth2-black?style=flat-square&logo=jsonwebtokens)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

---

## What is ShiftSync?

ShiftSync is a production-grade REST API backend for an internal HR operations portal, designed specifically for companies running **shift-based or on-call teams** — the kind of environment common in IT support, infrastructure, and managed services.

Instead of juggling spreadsheets and Slack messages, employees get one place to manage everything — their shift schedule, leave requests, work-from-home days, shift swaps with teammates, and on-call duties. The system handles all balance tracking, quota resets, and comp-off crediting automatically through background scheduled jobs, with zero manual HR intervention needed.

> This project was built to demonstrate production-quality Java backend development, directly drawing on real-world experience in a shift-based MNC environment.

---

## Features

### 🔐 Authentication
- Stateless JWT authentication with access tokens (24h) and refresh tokens (7 days)
- Google OAuth2 login (production-ready)
- Mock login endpoint for local development — auto-provisions employees with default leave balances on first login
- Role-based access control (EMPLOYEE / MANAGER / ADMIN)

### 🗓️ Shift Calendar
- Per-employee shift scheduling (Morning, Afternoon, Night, US Shift, General)
- Team calendar view — see who has which shift and at what time
- **US holiday overlay** — employees on US shifts see a banner when it is a US federal holiday
- India public holiday calendar integrated alongside US holidays

### 🌴 Leave Management
- Six leave types: Annual, Medical, Maternity, Paternity, Comp-Off, LOP (Loss of Pay)
- Default leave credits auto-assigned on employee onboarding
- Business rule enforcement: overlapping leave detection, balance validation, working day calculation
- Cancel pending leave requests with automatic balance restoration
- Team leave calendar view

### 🏠 Work From Home (WFH)
- Monthly WFH quota of 2 days per employee
- Automatic monthly reset via Spring @Scheduled job on the 1st of every month
- Duplicate WFH prevention for the same date
- Balance deduction on application, restoration on cancellation
- Team WFH calendar view

### 🔄 Shift Swap
- Two-stage approval workflow:
  - Stage 1: Target employee accepts or rejects the swap
  - Stage 2: Mock manager auto-approval
- Validates both employees have shifts on the requested dates before allowing the swap
- On approval, shift assignments are actually swapped in the database
- View incoming swap requests (where you are the target) and outgoing requests (where you are the requester)

### ⏰ Comp-Off Management
- Daily scheduled job checks if yesterday's on-call duty fell on a holiday or weekend
- Automatically credits 1 comp-off day to the on-call employee (primary + secondary) — no manual tracking needed
- Comp-off credits expire after 3 months (configurable)
- When applying for leave on a holiday, employees get an option to use comp-off credits instead of regular leave
- View all active comp-off credits with days-until-expiry

### 📞 On-Call Roster
- View upcoming on-call duties (primary and secondary)
- Acknowledge on-call assignments with timestamp tracking
- On-call roster view for the full team across a date range
- On-call entries on holidays/weekends automatically feed into the comp-off auto-credit system

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.3.4 |
| Security | Spring Security + JWT + OAuth2 (Google) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Cache | Redis (holiday calendar, team shift views) |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5 + Mockito + Testcontainers |
| Build | Maven |
| Local Dev | Docker Compose (PostgreSQL + Redis) |
| Deployment | Railway (backend) + Vercel (frontend) |

---

## Architecture

```
+--------------------------------------------------+
|              React Frontend (Vercel)             |
|     Calendar . Leave Forms . Swap UI . WFH      |
+------------------+-------------------------------+
                   | REST API (JWT Bearer)
+------------------v-------------------------------+
|           Spring Boot 3 API (Railway)            |
|                                                  |
|  +----------+ +----------+ +------------------+  |
|  |  Auth    | |  Leave   | |  Shift + Swap    |  |
|  |  Module  | |  Module  | |  Module          |  |
|  +----------+ +----------+ +------------------+  |
|  +----------+ +----------+ +------------------+  |
|  |  WFH     | | Comp-Off | |  On-Call Module  |  |
|  |  Module  | |  Module  | |                  |  |
|  +----------+ +----------+ +------------------+  |
|                                                  |
|  +---------------------------------------------+ |
|  |  Scheduled Jobs (@Scheduled)                | |
|  |  . Monthly WFH reset (1st of month)         | |
|  |  . Daily comp-off auto-credit (6 AM)        | |
|  +---------------------------------------------+ |
+----------+---------------------------+-----------+
           |                           |
+----------v----------+  +-------------v----------+
|   PostgreSQL        |  |   Redis                |
|   (Primary DB)      |  |   (Cache)              |
+---------------------+  +------------------------+
```

---

## Project Structure

```
src/main/java/com/shiftsync/
├── ShiftSyncApplication.java
├── config/          # Security, Redis, OpenAPI configuration
├── controller/      # REST controllers (thin layer, delegates to services)
├── dto/
│   ├── request/     # Validated request bodies
│   └── response/    # API response shapes (entities never exposed directly)
├── entity/          # JPA entities (10 tables)
├── enums/           # LeaveType, ShiftType, RequestStatus, SwapStatus, Role
├── exception/       # Global exception handler (RFC 7807 ProblemDetail)
├── repository/      # Spring Data JPA repositories with custom JPQL queries
├── scheduler/       # WFH monthly reset + comp-off daily auto-credit
├── security/        # JWT provider, auth filter, UserPrincipal
└── service/         # Business logic layer

src/main/resources/
├── application.yml
└── db/migration/
    ├── V1__initial_schema.sql    # All tables + indexes
    └── V2__seed_holidays.sql     # India + US holidays 2025
```

---

## API Endpoints

| Module | Method | Endpoint | Description |
|---|---|---|---|
| **Auth** | POST | `/api/v1/auth/mock-login` | Dev login, auto-provisions user |
| | POST | `/api/v1/auth/refresh` | Refresh access token |
| | GET | `/api/v1/auth/me` | Current user profile |
| **Leave** | GET | `/api/v1/leaves/balances` | All leave balances |
| | GET | `/api/v1/leaves` | My leave history (paginated) |
| | POST | `/api/v1/leaves` | Apply for leave |
| | PATCH | `/api/v1/leaves/{id}/cancel` | Cancel pending leave |
| | GET | `/api/v1/leaves/team` | Team leave calendar |
| **WFH** | GET | `/api/v1/wfh/balance` | Current month WFH balance |
| | POST | `/api/v1/wfh` | Apply for WFH |
| | PATCH | `/api/v1/wfh/{id}/cancel` | Cancel pending WFH |
| | GET | `/api/v1/wfh/team` | Team WFH calendar |
| **Shifts** | GET | `/api/v1/shifts/my` | My shift schedule |
| | GET | `/api/v1/shifts/today` | Today's shift |
| | GET | `/api/v1/shifts/team` | Team shift calendar |
| **Shift Swap** | POST | `/api/v1/shift-swaps` | Request a swap |
| | GET | `/api/v1/shift-swaps/incoming` | Swaps awaiting my response |
| | PATCH | `/api/v1/shift-swaps/{id}/respond` | Accept or reject a swap |
| | PATCH | `/api/v1/shift-swaps/{id}/cancel` | Cancel my swap request |
| **Comp-Off** | GET | `/api/v1/comp-off/credits` | My active comp-off credits |
| **Holidays** | GET | `/api/v1/holidays` | All holidays (date range) |
| | GET | `/api/v1/holidays/india` | India holidays |
| | GET | `/api/v1/holidays/us` | US federal holidays |
| | GET | `/api/v1/holidays/today` | Is today a holiday? |
| **On-Call** | GET | `/api/v1/on-call/upcoming` | My upcoming on-call duties |
| | GET | `/api/v1/on-call/roster` | Full team on-call roster |
| | PATCH | `/api/v1/on-call/{id}/acknowledge` | Acknowledge on-call duty |

---

## Local Development Setup

### Prerequisites
- Java 21
- Maven 3.9+
- Docker Desktop

### 1. Clone the repo
```bash
git clone https://github.com/YOUR_USERNAME/shiftsync.git
cd shiftsync
```

### 2. Start PostgreSQL and Redis
```bash
docker-compose up -d
```

### 3. Run the application
```bash
mvn spring-boot:run
```

### 4. Open Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### 5. Get a token and start testing
```json
POST /api/v1/auth/mock-login
{
  "email": "test@shiftsync.com",
  "name": "Test User"
}
```
Copy the `accessToken`, click **Authorize** in Swagger, paste it in — all endpoints are now available.

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/shiftsync` | PostgreSQL connection URL |
| `DB_USERNAME` | `shiftsync_user` | Database username |
| `DB_PASSWORD` | `shiftsync_pass` | Database password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `JWT_SECRET` | _(set in yml)_ | 64-char hex secret for JWT signing |
| `SPRING_PROFILE` | `mock` | `mock` for local dev, `prod` for production |
| `PORT` | `8080` | Server port |

---

## Key Design Decisions

- **DTOs at the boundary** — JPA entities are never exposed directly in API responses. All responses go through dedicated response DTOs.
- **Business logic in services** — controllers are intentionally thin, only handling request parsing and response formatting.
- **Idempotent schedulers** — the comp-off auto-credit job checks for existing credits before creating new ones, preventing double-crediting even if the job runs twice.
- **Spring Profiles** — `mock` profile uses stub OAuth and auto-provisioning for local dev; `prod` profile uses real Google OAuth2 and SMTP.
- **RFC 7807 error responses** — all errors return a consistent `ProblemDetail` JSON structure, never raw stack traces.

---

## Author

Built by Shruti Sinha — Linux Administrator @Wipro 
Connecting infrastructure operations experience with modern backend engineering.

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?style=flat-square&logo=linkedin)](https://www.linkedin.com/in/shruti-sinha1/)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black?style=flat-square&logo=github)](https://github.com/shruti1803)
