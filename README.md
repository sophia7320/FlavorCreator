<p align="center">
  <img src="art/logo.png" alt="FlavorCreator Logo" width="120" onerror="this.style.display='none'">
</p>

<h1 align="center">FlavorCreator <small>（创味机）</small></h1>

<p align="center">
  <strong>微信小程序后端 —— 菜谱发现、AI 智能生成、食材管理与社区分享</strong>
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

## 项目概述

FlavorCreator 是支撑微信小程序的后端服务，帮助用户发现菜谱、AI 智能生成菜谱、管理厨房食材以及参与美食社区互动。基于 Spring Boot 构建，采用清晰的分层架构。

> **注意：** 本仓库仅包含**后端**（`backend/`）代码。微信小程序前端（`miniprogram/`）是独立的原生项目，不在此仓库维护。

## 功能特性

| 模块 | 说明 |
|------|------|
| **Auth**（`auth`） | 微信一键登录、JWT 签发、刷新令牌管理 |
| **Recipe**（`recipe`） | 菜谱发布、AI 智能生成（LLM）、带筛选的列表浏览、详情查看、食材匹配推荐 |
| **Community**（`community`） | 菜谱点赞、收藏、评论 |
| **Ingredient**（`ingredient`） | 食材管理 — 食材与调料的 CRUD、过期提醒、常用食材预设 |
| **User**（`user`） | 个人资料管理、偏好设置、头像上传、我的收藏/点赞/菜谱 |
| **Common**（`common`） | 认证拦截器、AOP 日志、全局异常处理、文件存储（本地/COS/OSS）、图片审核 |

### 亮点

- **AI 菜谱生成**：对接 SiliconFlow LLM API，根据用户食材和偏好（口味、饮食限制、烹饪时长、难度）智能生成菜谱，含容错解析与字段类型校验
- **食材匹配推荐**：根据用户已有食材匹配系统菜谱，计算匹配度排序推荐
- **双 JWT 认证**：用户端（`/api/**`）使用 `@Public` 注解控制公开/需认证，通过 `AuthInterceptor` 统一拦截
- **图片审核流水线**：三步上传 — `validate`（类型+大小）→ `store`（上传）→ `moderate`（内容审核）；dev 跳过审核，cloud/prod 使用腾讯云 COS CI
- **令牌存储灵活**：默认内存（`ConcurrentHashMap`），可选 Redis，通过 `flcr.token.store` 切换
- **分层架构**：`Controller → Service（接口）→ ServiceImpl → Mapper`，严格遵守关注点分离
- **统一响应**：所有 API 返回 `Response<T> { code, message, data }` 格式

## 技术栈

| 层级 | 技术 |
|------|------|
| **语言** | Java 17 |
| **框架** | Spring Boot 4.0.4 |
| **ORM** | MyBatis-Plus 3.5.16 |
| **数据库** | MySQL 8.0 |
| **缓存** | Redis 7（可选） |
| **认证** | JWT（java-jwt 4.4.0 / Auth0） |
| **微信 SDK** | WxJava（weixin-java-miniapp）4.8.0 |
| **文件存储** | 本地 / 腾讯云 COS / 阿里云 OSS |
| **图片审核** | 腾讯云 COS CI（云端/生产） |
| **API 文档** | SpringDoc OpenAPI（Swagger UI）2.7.0 |
| **构建** | Maven Wrapper（`./mvnw`） |
| **容器化** | Docker 多阶段构建 + docker-compose |

## 快速开始

### 前置条件

- Java 17+
- Maven 3.9+（或使用内置 `./mvnw`）
- MySQL 8.0
- Redis 7（可选 — 可使用内存令牌存储）

### 方式一：Docker Compose（推荐）

一键启动 MySQL 8.0、Redis 7 和应用：

```bash
docker compose up -d --build
```

应用将在 `http://localhost:8080` 启动。

### 方式二：本地开发

```bash
# 1. 创建数据库
mysql -u root -e "CREATE DATABASE IF NOT EXISTS flcr CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 运行 SQL 初始化脚本
mysql -u root flcr < script/sql/flcr.sql
mysql -u root flcr < script/sql/user.sql
mysql -u root flcr < script/sql/recipe.sql
mysql -u root flcr < script/sql/ingredient.sql
mysql -u root flcr < script/sql/community.sql

# 3. 构建并运行
cd backend
./mvnw spring-boot:run
```

### 环境配置

| 环境 | 配置文件 | 说明 |
|------|----------|------|
| `dev` | `application-dev.yml` | 本地开发（默认） |
| `cloud` | `application-cloud.yml` | 微信云托管 |
| `prod` | `application-prod.yml` | 生产（环境变量驱动） |

```bash
# 切换环境
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

## 项目结构

```
FlavorCreator/
├── backend/                                    # Spring Boot 后端
│   ├── src/main/java/flcr/backend/
│   │   ├── BackendApplication.java             # 入口 + @MapperScan
│   │   ├── auth/                               # 认证模块
│   │   │   ├── controller/                     #   REST 控制器
│   │   │   ├── service/                        #   服务接口 + impl/
│   │   │   ├── mapper/                         #   MyBatis-Plus 映射器
│   │   │   ├── entity/                         #   数据库实体
│   │   │   └── DTO/                            #   请求/响应 DTO
│   │   ├── community/                          # 社区模块
│   │   ├── recipe/                             # 菜谱模块（含 LLM 客户端）
│   │   │   ├── client/                         #   LlmClient（AI 接口）
│   │   │   ├── controller/                     #   RecipeController
│   │   │   ├── service/                        #   RecipeService + RecipeGenerateService
│   │   │   └── DTO/                            #   请求/响应 DTO
│   │   ├── ingredient/                         # 食材模块
│   │   ├── user/                               # 用户模块
│   │   └── common/                             # 公共组件
│   │       ├── aop/                            #   AuthInterceptor + LoggingAspect
│   │       ├── config/                         #   Spring 配置（WebMvc、CORS、Jackson）
│   │       ├── constants/                      #   ResultCode 错误码
│   │       ├── context/                        #   UserContext（ThreadLocal）
│   │       ├── exception/                      #   BusinessException + 全局处理器
│   │       ├── response/                       #   统一响应 Response<T>
│   │       ├── service/                        #   文件存储、图片审核
│   │       └── util/                           #   JwtTokenUtil、PasswordUtil
│   ├── src/main/resources/
│   │   ├── application.yml                     # 基础配置
│   │   ├── application-dev.yml                 # 开发环境
│   │   ├── application-cloud.yml               # 微信云托管
│   │   ├── application-prod.yml               # 生产环境
│   │   └── logback-spring.xml                  # 日志配置
│   ├── src/test/                               # 216 条测试用例
│   ├── Dockerfile                              # 多阶段构建
│   └── pom.xml                                 # Maven 项目文件
├── script/sql/                                 # 数据库初始化脚本
├── doc/                                        # 技术文档
├── docker-compose.yml                          # Docker Compose 编排
└── miniprogram/                                # 微信小程序前端（不在此维护）
```

## API 文档

应用运行后，交互式 API 文档位于：

```
http://localhost:8080/swagger-ui.html
```

### API 概览

| 前缀 | 作用范围 | 认证方式 |
|------|----------|----------|
| `/api/auth` | 微信登录、令牌刷新、登出 | `@Public` |
| `/api/recipe` | CRUD、AI 生成、列表、详情、食材匹配 | 混合 |
| `/api/community` | 点赞、收藏、评论 | 需 JWT |
| `/api/ingredient` | 食材管理、过期提醒 | 需 JWT |
| `/api/user` | 个人资料、偏好、上传 | 需 JWT |

### 关键端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login-wx` | `POST` | 微信登录 |
| `/api/auth/refresh` | `POST` | 刷新 Token |
| `/api/recipe/generate` | `POST` | AI 菜谱生成 |
| `/api/recipe/apply` | `POST` | 食材匹配推荐 |
| `/api/user/recipes` | `GET` | 我的发布 |
| `/api/user/likes` | `GET` | 我的点赞 |
| `/api/user/collections` | `GET` | 我的收藏 |
| `/api/image/upload` | `POST` | 通用图片上传 |

所有接口返回统一 `Response<T>` 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## 架构

### 分层设计

```
┌──────────────────────────────────────────────────┐
│  Controller（@RestController）                     │
│  → 接收 HTTP 请求，校验参数                         │
│  → 调用 Service，返回 Response<T>                   │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│  Service（接口）/ ServiceImpl                      │
│  → 业务逻辑，@Transactional 写操作                  │
│  → 通过 UserContext（ThreadLocal）获取 userId       │
│  → 出错时抛出 BusinessException                    │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│  Mapper（继承 BaseMapper<Entity>）                 │
│  → 通过 MyBatis-Plus 操作数据库                    │
└──────────────────────────────────────────────────┘
```

### 认证流程

```
客户端                    AuthInterceptor          服务层
  │                              │                      │
  │  Authorization: Bearer <JWT> │                      │
  │─────────────────────────────>│                      │
  │                              │ 解析 JWT              │
  │                              │ 设置 UserContext      │
  │                              │─────────────────────>│
  │                              │                      │ UserContext.getUserId()
  │                              │                      │
  │  Response<T>                 │                      │
  │<─────────────────────────────│  afterCompletion      │
  │                              │ UserContext.clear()   │
```

- 标注 `@Public` 的方法无需令牌即可访问
- 未标注 `@Public` 的方法需要有效 JWT，否则返回 401

### AI 菜谱生成流程

```
用户                     Controller              Service               LlmClient          LLM API
  │                           │                      │                      │                 │
  │ POST /recipe/generate     │                      │                      │                 │
  │ { ingredients, prefs }    │                      │                      │                 │
  │──────────────────────────>│                      │                      │                 │
  │                           │ buildSystemPrompt()   │                      │                 │
  │                           │─────────────────────>│                      │                 │
  │                           │                      │ generateRecipeJson() │                 │
  │                           │                      │─────────────────────>│ POST /chat      │
  │                           │                      │                      │────────────────>│
  │                           │                      │                      │<─── JSON ───────│
  │                           │                      │ parseResponse()      │                 │
  │                           │                      │ (容错: 正则提取JSON)   │                 │
  │                           │                      │ (校验: recipe!=null)  │                 │
  │  200 { recipe }           │                      │                      │                 │
  │<──────────────────────────│<─────────────────────│                      │                 │
```

## 测试

```bash
cd backend
./mvnw test                              # 运行全部测试（216 条）
./mvnw test -Dtest=RecipeServiceImplTest # 运行单个测试类
```

| 类型 | 注解 | 说明 |
|------|------|------|
| Mapper 集成测试 | `@SpringBootTest` + `@Transactional` | 真实数据库，自动回滚 |
| Service 单元测试 | `@ExtendWith(MockitoExtension.class)` | Mock Mapper |
| Controller 单元测试 | `@ExtendWith(MockitoExtension.class)` | Mock Service |
| 工具类纯单元测试 | 无 Spring 注解 | 纯 JUnit |

## 配置参考

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.profiles.active` | `dev` | 激活的环境配置 |
| `server.port` | `8080` | HTTP 端口 |
| `jwt.expiration` | `300000`（5 分钟） | 访问令牌有效期（毫秒） |
| `jwt.refresh-expiration` | `2592000000`（30 天） | 刷新令牌有效期（毫秒） |
| `flcr.storage.type` | `local` | 文件存储类型：`local` / `cos` / `oss` |
| `flcr.storage.local-path` | `./uploads` | 本地上传目录 |
| `flcr.token.store` | `memory` | 令牌存储方式：`memory` / `redis` |
| `flcr.moderation.enabled` | `false`（dev） | 是否启用图片内容审核 |
| `spring.servlet.multipart.max-file-size` | `10MB` | 单文件上传限制 |

## 生产环境变量

使用 `application-prod.yml` 时，所有敏感值通过环境变量注入：

| 变量 | 用途 |
|------|------|
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | MySQL 用户名 |
| `DB_PASSWORD` | MySQL 密码 |
| `WX_APP_ID` | 微信小程序 AppID |
| `WX_SECRET` | 微信小程序 AppSecret |
| `JWT_SECRET` | JWT 签名密钥 |
| `REDIS_URL` | Redis 连接地址（可选） |
| `LLM_API_KEY` | LLM API 密钥 |
| `COS_SECRET_ID` | 腾讯云 COS SecretId |
| `COS_SECRET_KEY` | 腾讯云 COS SecretKey |

## 部署

### Docker

```bash
docker compose up -d --build
```

Dockerfile 采用多阶段构建：
1. **构建阶段**：基于 `maven:3.9-eclipse-temurin-17-alpine` 编译项目
2. **运行阶段**：将 JAR 复制到 `eclipse-temurin:17-jre`，获得最小镜像

### 微信云托管

设置 `SPRING_PROFILES_ACTIVE=cloud`，通过云控制台配置环境变量。

## 贡献指南

1. 遵循 `AGENTS.md` 中的编码规范
2. 为新功能编写测试
3. 确保所有测试通过：`./mvnw test`
4. 提交 Pull Request

### 编码规范

详细架构指南与编码标准请参考 `AGENTS.md`，包括：

- Entity / Mapper / DTO / Service / Controller 模板
- 错误处理：使用 `BusinessException`，禁止直接抛出 `RuntimeException`
- 跨模块引用：直接注入 **Mapper**，而非 Service（避免循环依赖）
- 公开接口使用 `@Public` 注解，默认需认证

## 开源协议

本项目基于 MIT 协议开源，详情见 [LICENSE](LICENSE) 文件。
