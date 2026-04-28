# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

FlavorCreator (创味机) - 基于 Spring Boot 的微信小程序后端应用，提供食谱推荐、食材管理、社区分享等功能。

## 构建与运行

所有命令在 `backend/` 目录下执行：

```bash
# 构建（跳过测试）
./mvnw clean package -DskipTests

# 运行应用
./mvnw spring-boot:run

# 运行所有测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=BackendApplicationTests
./mvnw test -Dtest=UserMapperTest
```

## 技术栈

- **框架**: Spring Boot 4.0.4 + Java 17
- **ORM**: MyBatis-Plus 3.5.16
- **数据库**: MySQL 8.0.31
- **微信 SDK**: WxJava SDK 4.8.0（微信小程序）
- **认证**: JWT (java-jwt 4.4.0)
- **缓存**: Redis (spring-boot-starter-data-redis)
- **AOP**: spring-aop + aspectjweaver（方法调用日志）

## 架构结构

### 包结构
```
flcr.backend/
├── auth/       # 用户认证模块（微信登录、token 刷新，手机号绑定已移除）- 已实现
├── admin/      # 管理员模块 - 待实现
├── community/  # 社区模块（菜谱发布、点赞收藏、评论）- 已实现
├── recipe/     # 食谱模块（AI生成、食材匹配、推荐搜索）- 实体+Mapper 就绪，Controller/Service 待开发
├── ingredient/ # 食材管理模块（食材/调味品 CRUD、临期提醒、常用食材库）- 已实现
├── common/     # 公共组件
│   ├── aop/         # AOP 切面（AuthAspect 认证、LoggingAspect 日志）
│   ├── config/      # 配置类（WxMa、Jackson、MyBatis）
│   ├── constants/   # 常量定义（ResultCode）
│   ├── context/     # 上下文（UserContext ThreadLocal）
│   ├── exception/   # 异常类（BusinessException、GlobalExceptionHandler）
│   ├── service/      # 通用服务（SmsService 短信验证码）
│   ├── response/    # 统一响应类（Response<T>）
│   └── util/        # 工具类（JwtTokenUtil）
└── Test/       # 测试控制器
```

### 模块设计模式

**Controller 层**:
- 使用 `@Slf4j` + `@RestController` + `@RequestMapping("/api/模块")`
- 统一返回 `Response<T>` 泛型类（位于 `common.response` 包）
- 构造器注入依赖（Lombok `@RequiredArgsConstructor`）
- 需要认证的方法标记 `@RequireAuth`，Service 层通过 `UserContext.getUserId()` 获取当前用户 ID
- 请求体通过 `@RequestBody` DTO 接收，multipart 用 `@RequestPart` 绑定 JSON 部分

**Service 层**:
- 接口定义在 `service/` 目录
- 实现在 `service/impl/` 目录
- 使用 `@RequiredArgsConstructor` 注入 Mapper 和其他依赖
- 写操作使用 `@Transactional`

**Mapper 层**:
- 继承 `BaseMapper<Entity>` 获得 CRUD 能力
- 复杂查询使用 `LambdaQueryWrapper`

**Entity 层**:
- 使用 `@TableName` 指定表名（`autoResultMap=true` 支持 JSON 列）
- 使用 `@TableId(type = IdType.AUTO)` 指定主键自增
- Lombok `@Data` 简化样板代码
- JSON 字段使用 `String` 类型存储，通过 `ObjectMapper` 序列化/反序列化

**DTO 分层**:
- `DTO/request/` - 请求 DTO
- `DTO/response/` - 响应 DTO
- 使用 Lombok `@Builder` 构建复杂响应对象

### 数据库表

**auth 模块**:
- `user` - 用户表（id, openid, unionid, nickname, avatar, phone_number 等）

**建表脚本** (`script/sql/`):
- `flcr.sql` — 建库语句
- `user.sql` — 用户表
- `recipe.sql` — 菜谱表（name, cover, images, author_id, ingredients, steps, tags, category, like_count, collection_count, comment_count, view_count 等）
- `community.sql` — 社区模块表
  - `comment` — 评论表（user_id, recipe_id, parent_id, content, like_count）
  - `like` — 点赞表（user_id, target_type[1=菜谱/2=评论], target_id，唯一约束）
  - `collection` — 收藏表（user_id, recipe_id，唯一约束）
- `ingredient.sql` — 食材模块表
  - `ingredient` — 食材表（user_id, name, quantity, unit, category, storage_condition, expire_date）
  - `common_ingredient` — 常用食材表（系统预设，category, name, default_unit）
- `menu.sql` — [废弃] 旧版菜谱设计，保留供参考

### 核心组件

**WxMaConfiguration** (`common.config`):
- 配置微信小程序 SDK
- 支持微信云托管环境（从文件路径读取 access_token）
- 提供 `WxMaService` Bean

**JacksonConfig** (`common.config`):
- 手动创建 `@Bean ObjectMapper`，禁用 `WRITE_DATES_AS_TIMESTAMPS`
- 用于 Entity 中 JSON 字段的序列化/反序列化
- 注：Spring Boot 4 默认使用 `tools.jackson` 命名空间，此 Bean 为 `com.fasterxml.jackson` 桥接

**MyBatisConfig** (`common.config`):
- MyBatis 配置类（预留分页插件等配置入口）
- Mapper 扫描由 `BackendApplication` 上的 `@MapperScan` 负责

**CorsConfig** (`common.config`):
- 实现 `WebMvcConfigurer`，允许 `/api/**` 所有来源跨域
- 支持 GET/POST/PUT/DELETE/OPTIONS，允许携带 Cookie

**LoggingAspect** (`common.aop`):
- AOP 环绕通知，记录所有 Controller 和 Service 方法调用
- 输出：类名、方法名、入参、返回值、执行耗时
- 异常时输出错误信息和耗时

**AuthAspect** (`common.aop`, @Order(1)):
- 拦截 `@RequireAuth` 标记的方法，从 `Authorization: Bearer <token>` 中提取 JWT
- Token 校验后查 Redis 黑名单（`TokenBlacklistService.isBlacklisted()`）
- 验证通过后将 userId 和 jti 写入 `UserContext`（ThreadLocal）
- `@RequireAuth(required = false)` 时 Token 可选，有则解析，无则放行

**RequireAuth** (`common.aop`):
- 标记需要 Token 认证的 Controller 方法

**UserContext** (`common.context`):
- ThreadLocal 持有当前请求的 userId 和 jti（Token 唯一标识，用于黑名单查询）
- AuthAspect 在请求结束后自动 clear

**BusinessException** (`common.exception`):
- 业务异常类，携带 `code` 状态码
- 构造器：`BusinessException(code, message)` 或 `BusinessException(message)`（默认 500）
- **Service 层必须使用 `BusinessException`，禁止直接 `throw new RuntimeException()`**，否则 `GlobalExceptionHandler` 无法返回业务错误码

**GlobalExceptionHandler** (`common.exception`):
- `@RestControllerAdvice` 全局异常处理器
- 捕获 `BusinessException` 返回对应 code 和 message
- 捕获 `MethodArgumentNotValidException` / `ConstraintViolationException` → 400（`@Valid` 校验失败）
- 捕获 `HttpMessageNotReadableException` → 400（JSON 格式错误）
- 捕获 `BindException` → 400（表单绑定失败）
- 捕获 `Exception` 返回 500 通用错误

**JwtTokenUtil** (`common.util`):
- `generateToken(userId, openid)` - 生成访问令牌（2h，含 jti UUID）
- `generateRefreshToken(userId, openid)` - 生成刷新令牌（7d，含 jti UUID）
- `validateToken(token)` - 验证令牌
- `getUserIdFromToken(token)` - 解析用户 ID
- `getOpenidFromToken(token)` - 解析 OpenID
- `getJtiFromToken(token)` - 解析 jti（JWT ID）
- `getRemainingTime(token)` - 获取 Token 剩余有效毫秒数

**Response** (`common.response`):
- 统一响应格式：`{code, message, data}`
- 静态工厂方法：`success()`, `success(T)`, `success(String, T)`, `error(code, msg)`

**TokenBlacklistService** (`common.service`):
- Redis 黑名单管理 logout/refresh 后的 Token 失效
- `blacklist(jti)` — 将 jti 加入黑名单，TTL = Token 剩余有效时长
- `isBlacklisted(jti)` — 检查 jti 是否在黑名单中，Redis 不可用时自动放行

### API 接口

**Auth 模块** (`/api/auth`):

1. `POST /api/auth/login-wx` - 微信一键登录
   - 入参：`LoginRequestDTO` (code, userInfo)
   - 出参：`Response<LoginResponseDTO>` (token, refreshToken, user)

2. `POST /api/auth/refresh` - 刷新 Token
   - 入参：`RefreshTokenRequestDTO` (refreshToken)
   - 出参：`Response<LoginResponseDTO>`

3. `POST /api/auth/logout` - 退出登录（需认证，将 token 加入 Redis 黑名单）

**Community 模块** (`/api/community`):

所有写操作和互动接口需要 `Authorization: Bearer <token>` 请求头。

1. `POST /api/community/recipe` - 发布菜谱（multipart/form-data，需认证）
   - 表单字段：`@RequestPart("request")` DTO + `@RequestParam` 文件

2. `GET /api/community/recipe/list` - 菜谱列表（公开，分页 + 筛选）
   - 参数：category, difficulty, taste, keyword, page, size

3. `GET /api/community/recipe/{id}` - 菜谱详情（游客可访问，登录则回传点赞/收藏状态）

4. `POST /api/community/recipe/{id}/like` - 点赞菜谱（需认证）
5. `DELETE /api/community/recipe/{id}/like` - 取消点赞（需认证）
6. `POST /api/community/recipe/{id}/collect` - 收藏菜谱（需认证）
7. `DELETE /api/community/recipe/{id}/collect` - 取消收藏（需认证）
8. `GET /api/community/recipe/{id}/comment` - 评论列表（游客可访问）
9. `POST /api/community/recipe/{id}/comment` - 发表评论（需认证）
10. `DELETE /api/community/comment/{id}` - 删除评论（需认证，仅本人）
11. `POST /api/community/comment/{id}/like` - 点赞评论（需认证）
12. `DELETE /api/community/comment/{id}/like` - 取消点赞评论（需认证）

**Ingredient 模块** (`/api/ingredient`):

1. `GET /api/ingredient/list` - 食材列表（需认证，支持 sortBy/sort/status/category 筛选）
2. `POST /api/ingredient` - 添加食材（需认证）
3. `PUT /api/ingredient/{id}` - 更新食材（需认证，仅本人）
4. `DELETE /api/ingredient/{id}` - 删除食材（需认证，仅本人）
5. `POST /api/ingredient/batch` - 批量添加食材（需认证）
6. `GET /api/ingredient/expiring-notice` - 临期/过期提醒（需认证）
7. `GET /api/ingredient/common` - 常用食材分类列表（游客可访问）

**Condiment 复用** (`/api/condiment`):
- POST/GET/PUT/DELETE 调味品接口，复用 IngredientService，category 固定为"调味品"

### 错误码定义 (`common.constants.ResultCode`)

| 码 | 说明 |
|----|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 用户不存在/Token 过期 |
| 402 | 用户已存在 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 系统错误 |
| 1001 | 微信 code 无效 |
| 1002 | 微信接口调用失败 |
| 1003 | 手机号获取失败 |

## 配置说明

三个配置文件按环境分离：

- **`application.yml`** — 共用配置（应用名、端口、JWT 过期时长、文件上传大小限制、Actuator 端点、MyBatis 驼峰映射）
- **`application-dev.yml`** — 开发环境（本地 MySQL + HikariCP 连接池、Redis 超时、SQL 日志打印、项目包 DEBUG 日志）
- **`application-prod.yml`** — 生产环境（数据库/微信/JWT 均通过 `${ENV_VAR}` 注入、HikariCP 连接池(20)、日志文件写入 `/var/log/flavor-creator`）
- **`logback-spring.xml`** — 日志配置（控制台输出 + 按日滚动文件 + ERROR 单独文件，保留 30 天）

## 测试

```bash
# 所有测试（UserMapper 9 + IngredientMapper 9 + CommonIngredientMapper 4 + ResultCode 3 + BusinessException 4 + Response 7 + JwtTokenUtil 13 + Application 1 = 50 个）
./mvnw test

# 单个测试类
./mvnw test -Dtest=JwtTokenUtilTest
./mvnw test -Dtest=ResponseTest
./mvnw test -Dtest=BusinessExceptionTest
./mvnw test -Dtest=ResultCodeTest
./mvnw test -Dtest=UserMapperTest
./mvnw test -Dtest=IngredientMapperTest
./mvnw test -Dtest=CommonIngredientMapperTest
```

## 注意事项

- 数据库 `flcr` 需要手动创建，建表脚本在 `script/sql/` 目录
- Ingredient 模块已完整实现，logout 已支持 Redis 黑名单 Token 失效
- Community 模块中点赞评论（`likeComment`/`unlikeComment`）为 TODO 状态，待完善
- 菜谱的图片上传逻辑为占位符，实际文件存储（OSS）待实现
- Admin 和 Ingredient 模块尚未实现
- 认证通过 `@RequireAuth` + `AuthAspect` 实现，前端需传 `Authorization: Bearer <token>`
- `@MapperScan` 仅在 `BackendApplication` 上定义（`"flcr.backend.*.mapper"`），Mapper 接口无需 `@Mapper` 注解
- `JacksonConfig` 手动创建 `ObjectMapper` Bean 作为 Spring Boot 4 `tools.jackson` 的桥接
- Controller 入参应加 `@Valid` 注解启用 DTO 字段校验，失败由 `GlobalExceptionHandler` 统一返回 400
- Controller 不手动 try-catch，依赖 `GlobalExceptionHandler` 统一处理
- Service 层通过 `UserContext.getUserId()` 获取当前用户，不在方法签名中传 userId