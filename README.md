<p align="center">
  <img src="art/logo.png" alt="FlavorCreator Logo" width="120" onerror="this.style.display='none'">
</p>

<h1 align="center">FlavorCreator <small>(创味机)</small></h1>

<p align="center">
  <strong>A WeChat Mini Program Backend for Recipe Discovery, Ingredient Management, and Community Sharing</strong>
</p>

<p align="center">
  <a href="#license"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License"></a>
  <a href="https://www.java.com"><img src="https://img.shields.io/badge/Java-17-%23ED8B00?logo=openjdk&logoColor=white" alt="Java 17"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-4.0.4-%236DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4.0.4"></a>
  <a href="https://baomidou.com"><img src="https://img.shields.io/badge/MyBatis--Plus-3.5.16-%23C90016" alt="MyBatis-Plus 3.5.16"></a>
  <a href="https://www.mysql.com"><img src="https://img.shields.io/badge/MySQL-8.0-%234479A1?logo=mysql&logoColor=white" alt="MySQL 8.0"></a>
  <a href="https://redis.io"><img src="https://img.shields.io/badge/Redis-7-%23DC382D?logo=redis&logoColor=white" alt="Redis 7"></a>
  <a href="https://www.docker.com"><img src="https://img.shields.io/badge/Docker-%E2%9C%93-%232496ED?logo=docker&logoColor=white" alt="Docker"></a>
</p>

---

## Overview （项目概述）

FlavorCreator is a backend service powering a WeChat Mini Program that helps users discover recipes, manage kitchen ingredients, and engage with a food-loving community. Built with Spring Boot, it features a clean layered architecture, dual JWT authentication (user + admin), and flexible multi-environment deployment.

> **Note:** This repository covers the **backend** (`backend/`) only. The WeChat Mini Program frontend (`miniprogram/`) is a separate native project and is not maintained within this repo.

## Features （功能特性）

| Module | Description |
|--------|-------------|
| **Auth** (`auth`) | WeChat one-tap login (微信一键登录), JWT issuance, refresh token management |
| **Admin** (`admin`) | Admin dashboard — full CRUD for recipes & comments, user management, statistics, independent JWT auth |
| **Community** (`community`) | Recipe likes, favorites (collections), comments with toggle-mode atomic increment/decrement |
| **Recipe** (`recipe`) | Recipe publishing with multipart image upload, listing with filtering, detail view |
| **Ingredient** (`ingredient`) | Pantry management — CRUD for ingredients & condiments, expiration alerts, common ingredient presets |
| **User** (`user`) | Profile management, preferences, avatar/background image upload, my collections/likes/recipes |
| **Common** (`common`) | Auth interceptors, AOP logging, global exception handling, file storage (Local/COS/OSS), image moderation |

### Highlights

- **Dual JWT Auth**: Separate secret keys for user (`/api/**`) and admin (`/api/admin/**`) endpoints — no cross-access possible
- **Token Store Flexibility**: In-memory (`ConcurrentHashMap`) by default; Redis as an opt-in alternative via `flcr.token.store` config
- **Image Moderation Pipeline**: 3-step upload — `validate` (type + size) → `store` (upload) → `moderate` (content review); dev skips moderation, cloud/prod uses Tencent COS CI
- **Multi-Environment Configs**: `dev` (local), `cloud` (WeChat Cloud Run / 微信云托管), `prod` (production)
- **Layered Architecture**: `Controller → Service (interface) → ServiceImpl → Mapper` with strict adherence to separation of concerns
- **Unified Response**: `Response<T> { code, message, data }` for all API responses

## Tech Stack （技术栈）

| Layer | Technology |
|-------|------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 4.0.4 |
| **ORM** | MyBatis-Plus 3.5.16 |
| **Database** | MySQL 8.0 |
| **Cache** | Redis 7 (optional) |
| **Auth** | JWT (java-jwt 4.4.0 / Auth0) |
| **WeChat SDK** | WxJava (weixin-java-miniapp) 4.8.0 |
| **File Storage** | Local / Tencent COS / Alibaba OSS |
| **Image Moderation** | Tencent COS CI (cloud/prod) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) 2.7.0 |
| **Build** | Maven Wrapper (`./mvnw`) |
| **Containerization** | Docker multi-stage build + docker-compose |

## Quick Start （快速开始）

### Prerequisites

- Java 17+
- Maven 3.9+ (or use the bundled `./mvnw` wrapper)
- MySQL 8.0
- Redis 7 (optional — can run with in-memory token store)

### Option 1: Docker Compose (Recommended)

Starts MySQL 8.0, Redis 7, and the application in one command:

```bash
docker compose up -d --build
```

The app will be available at `http://localhost:8080`.

### Option 2: Local Development

```bash
# 1. Create the database
mysql -u root -e "CREATE DATABASE IF NOT EXISTS flcr CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Run SQL init scripts (optional — tables are auto-created by MyBatis-Plus in dev)
# mysql -u root flcr < script/sql/flcr.sql
# mysql -u root flcr < script/sql/user.sql
# ... (run all scripts under script/sql/)

# 3. Configure database credentials in backend/src/main/resources/application-dev.yml

# 4. Build & run
cd backend
./mvnw spring-boot:run
```

### Default Profiles

| Profile | Config File | Environment |
|---------|-------------|-------------|
| `dev` | `application-dev.yml` | Local development (default) |
| `cloud` | `application-cloud.yml` | WeChat Cloud Run (微信云托管) |
| `prod` | `application-prod.yml` | Production (env-variable driven) |

```bash
# Override profile
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

## Project Structure （项目结构）

```
FlavorCreator/
├── backend/                                    # Spring Boot backend
│   ├── src/main/java/flcr/backend/
│   │   ├── BackendApplication.java             # Entry point + @MapperScan
│   │   ├── auth/                               # Auth module
│   │   │   ├── controller/                     #   REST controllers
│   │   │   ├── service/                        #   Service interfaces
│   │   │   │   └── impl/                       #   Service implementations
│   │   │   ├── mapper/                         #   MyBatis-Plus mappers
│   │   │   ├── entity/                         #   DB entities
│   │   │   └── DTO/                            #   Request/Response DTOs
│   │   ├── admin/                              # Admin module
│   │   ├── community/                          # Community module
│   │   ├── recipe/                             # Recipe module
│   │   ├── ingredient/                         # Ingredient module
│   │   ├── user/                               # User module
│   │   └── common/                             # Shared components
│   │       ├── aop/                            #   Interceptors & aspects
│   │       ├── config/                         #   Spring configuration beans
│   │       ├── constants/                      #   Error codes (ResultCode)
│   │       ├── context/                        #   UserContext (ThreadLocal)
│   │       ├── exception/                      #   BusinessException & global handler
│   │       ├── response/                       #   Unified Response<T>
│   │       ├── service/                        #   Shared services (storage, moderation)
│   │       └── util/                           #   JwtTokenUtil & helpers
│   ├── src/main/resources/
│   │   ├── application.yml                     # Base configuration
│   │   ├── application-dev.yml                 # Development profile
│   │   ├── application-cloud.yml               # WeChat Cloud Run profile
│   │   ├── application-prod.yml                # Production profile
│   │   └── logback-spring.xml                  # Logging configuration
│   ├── src/test/                               # 165+ test cases
│   ├── Dockerfile                              # Multi-stage Docker build
│   └── pom.xml                                 # Maven project descriptor
├── script/sql/                                 # Database init scripts
├── docker-compose.yml                          # Docker Compose orchestration
└── miniprogram/                                # WeChat Mini Program frontend (not maintained here)
```

## API Documentation

Once the application is running, interactive API docs are available at:

```
http://localhost:8080/swagger-ui.html
```

### API Overview

| Prefix | Scope | Auth |
|--------|-------|------|
| `/api/auth` | WeChat login, token refresh, logout | Mixed (`@Public`) |
| `/api/recipe` | Recipe CRUD, listing, detail | Mixed (`@Public` for list/detail) |
| `/api/community` | Likes, favorites, comments | User JWT required |
| `/api/ingredient` | Pantry management, alerts | User JWT required |
| `/api/user` | Profile, preferences, uploads | User JWT required |
| `/api/admin` | Admin dashboard | Admin JWT required (separate secret) |

All endpoints return a unified `Response<T>` format:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## Architecture （架构）

### Layered Design

```
┌──────────────────────────────────────────────────┐
│  Controller (@RestController)                     │
│  → Receives HTTP requests, validates parameters   │
│  → Calls Service, returns Response<T>             │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│  Service (interface) / ServiceImpl                │
│  → Business logic, @Transactional writes          │
│  → Gets userId via UserContext (ThreadLocal)      │
│  → Throws BusinessException on errors             │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│  Mapper (extends BaseMapper<Entity>)              │
│  → Database operations via MyBatis-Plus            │
└──────────────────────────────────────────────────┘
```

### Auth Flow

```
Client                    AuthInterceptor         Service Layer
  │                              │                      │
  │  Authorization: Bearer <JWT> │                      │
  │─────────────────────────────>│                      │
  │                              │ parse JWT            │
  │                              │ set UserContext      │
  │                              │─────────────────────>│
  │                              │                      │ UserContext.getUserId()
  │                              │                      │
  │  Response<T>                 │                      │
  │<─────────────────────────────│  afterCompletion     │
  │                              │ UserContext.clear()   │
```

- Methods annotated with `@Public` are accessible without a token (guest-friendly)
- Methods without `@Public` require a valid JWT — returns 401 otherwise
- Admin endpoints (`/api/admin/**`) use a **separate JWT secret** — user tokens are rejected

## Testing （测试）

```bash
cd backend
./mvnw test                    # Run all tests (165+ cases)
./mvnw test -Dtest=UserMapperTest   # Run a single test class
```

| Type | Annotation | Scope |
|------|------------|-------|
| Mapper Integration | `@SpringBootTest` + `@Transactional` | Real DB, auto-rollback |
| Service Unit | `@ExtendWith(MockitoExtension.class)` | Mocked mappers |
| Controller Unit | `@ExtendWith(MockitoExtension.class)` | Mocked services |
| Utility Pure Unit | None | Plain JUnit |

## Configuration Reference （配置参考）

| Key | Default | Description |
|-----|---------|-------------|
| `spring.profiles.active` | `dev` | Active profile |
| `server.port` | `8080` | HTTP port |
| `jwt.expiration` | `300000` (5 min) | Access token TTL |
| `jwt.refresh-expiration` | `2592000000` (30 days) | Refresh token TTL |
| `flcr.storage.type` | `local` | File storage: `local` / `cos` / `oss` |
| `flcr.storage.local-path` | `./uploads` | Local upload directory |
| `flcr.token.store` | `memory` | Token store: `memory` / `redis` |
| `flcr.moderation.enabled` | `false` (dev) | Enable image content moderation |
| `spring.servlet.multipart.max-file-size` | `10MB` | Per-file upload limit |
| `spring.servlet.multipart.max-request-size` | `20MB` | Total request limit |

## Environment Variables (Production)

When using `application-prod.yml`, all sensitive values are injected via environment variables:

| Variable | Purpose |
|----------|---------|
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `WX_APP_ID` | WeChat Mini Program AppID |
| `WX_SECRET` | WeChat Mini Program AppSecret |
| `JWT_SECRET` | JWT signing secret (user) |
| `ADMIN_JWT_SECRET` | JWT signing secret (admin) |
| `REDIS_URL` | Redis connection URL (optional) |
| `COS_SECRET_ID` | Tencent COS SecretId (cloud/prod) |
| `COS_SECRET_KEY` | Tencent COS SecretKey (cloud/prod) |

## Deployment （部署）

### Docker

```bash
docker compose up -d --build
```

The Dockerfile uses a multi-stage build:
1. **Builder stage**: Compiles the project with Maven on `eclipse-temurin:17-alpine`
2. **Runtime stage**: Copies the JAR to `eclipse-temurin:17-jre-alpine` for a minimal image

### WeChat Cloud Run (微信云托管)

Set `SPRING_PROFILES_ACTIVE=cloud` and configure environment variables via the cloud console.

## Contributing （贡献指南）

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Follow the coding conventions in `AGENTS.md`
4. Write tests for new functionality
5. Ensure all tests pass: `./mvnw test`
6. Submit a Pull Request

### Coding Conventions

Refer to `AGENTS.md` for detailed architectural guidelines and coding standards, including:

- Entity / Mapper / DTO / Service / Controller templates
- Error handling: use `BusinessException` — never throw raw `RuntimeException`
- Cross-module access: inject **Mappers** directly, not Services (avoid circular dependencies)
- `@Public` annotation for public endpoints

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
