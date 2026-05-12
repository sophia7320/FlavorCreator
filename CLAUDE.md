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
├── recipe/     # 食谱模块（菜谱发布/列表/详情）- 已实现
├── ingredient/ # 食材管理模块（食材/调味品 CRUD、临期提醒、常用食材库）- 已实现
├── user/       # 用户信息模块（个人资料、偏好设置、头像/背景上传）- 已实现
├── common/     # 公共组件
│   ├── aop/         # AuthInterceptor 认证拦截器、LoggingAspect 日志、@Public 注解
│   ├── config/      # 配置类（WebMvcConfig 拦截器注册、WxMa、Jackson、StorageProperties、ModerationProperties）
│   ├── constants/   # 常量定义（ResultCode）
│   ├── context/     # 上下文（UserContext ThreadLocal）
│   ├── exception/   # 异常类（BusinessException、GlobalExceptionHandler）
│   ├── service/     # 通用服务（FileStorageService、ImageModerationService、RefreshTokenService）
│   ├── response/    # 统一响应类（Response<T>）
│   └── util/        # 工具类（JwtTokenUtil）
└── Test/       # 测试控制器
```

### 模块设计模式

**Controller 层**:
- 使用 `@Slf4j` + `@RestController` + `@RequestMapping("/api/模块")`
- 统一返回 `Response<T>` 泛型类（位于 `common.response` 包）
- 构造器注入依赖（Lombok `@RequiredArgsConstructor`）
- 需要认证的方法不加 `@Public` 注解（默认拦截）
- 公开方法标记 `@Public`（游客可访问，有 Token 则解析写入 UserContext）
- Service 层通过 `UserContext.getUserId()` 获取当前用户 ID
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

### 认证拦截器
**AuthInterceptor** (`common.aop`):
- 拦截所有 `/api/**` 请求，`WebMvcConfig` 注册
- 无 `@Public` 注解的方法 → 必须携带 `Authorization: Bearer <token>`，无则返回 401
- `@Public` 注解的方法 → Token 可选，有则解析写入 UserContext
- `afterCompletion()` 中自动 `UserContext.clear()`

**@Public** (`common.aop`):
- 标记无需强制认证的 Controller 方法（公开接口）

**LoggingAspect** (`common.aop`):
- AOP 环绕通知，记录所有 Controller 和 Service 方法调用
- 输出：类名、方法名、入参、返回值、执行耗时

**WxMaConfiguration** (`common.config`):
- 配置微信小程序 SDK
- 提供 `WxMaService` Bean

**JacksonConfig** (`common.config`):
- 手动创建 `@Bean ObjectMapper`，禁用 `WRITE_DATES_AS_TIMESTAMPS`

**WebMvcConfig** (`common.config`):
- 注册 `AuthInterceptor` 拦截所有 `/api/**` 请求

**CorsConfig** (`common.config`):
- 实现 `WebMvcConfigurer`，允许 `/api/**` 所有来源跨域

**UserContext** (`common.context`):
- ThreadLocal 持有当前请求的 userId
- AuthInterceptor 在请求结束后自动 clear

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
- `generateToken(userId, openid)` - 生成访问令牌（5min，含 jti UUID，实际未使用）
- `validateToken(token)` - 验证令牌（签名+过期）
- `getUserIdFromToken(token)` - 解析用户 ID
- `getOpenidFromToken(token)` - 解析 OpenID

**Response** (`common.response`):
- 统一响应格式：`{code, message, data}`
- 静态工厂方法：`success()`, `success(T)`, `success(String, T)`, `error(code, msg)`

**RefreshTokenService** (`common.service`):
- Redis 管理 Refresh Token，Key: `rt:{uuid}` → `{userId, openid}`
- `store(userId, openid, refreshToken)` — 存储 RT，TTL 30 天
- `get(refreshToken)` — 按 UUID 查询 RT 数据，返回 `RefreshTokenData{userId, openid}` 或 null
- `delete(refreshToken)` — 删除指定 RT
- Access Token 无状态（5min JWT），不需黑名单

### API 接口

**Auth 模块** (`/api/auth`):

1. `POST /api/auth/login-wx` - 微信一键登录
   - 入参：`LoginRequestDTO` (code, userInfo)
   - 出参：`Response<LoginResponseDTO>` (token, refreshToken, user)

2. `POST /api/auth/refresh` - 刷新 Token
   - 入参：`RefreshTokenRequestDTO` (refreshToken)
   - 出参：`Response<LoginResponseDTO>`

3. `POST /api/auth/logout` - 退出登录（需认证，传入 refreshToken 校验归属后删除）

**Recipe 模块** (`/api/recipe`):
菜谱 CRUD 从 Community 模块拆分独立：

1. `POST /api/recipe` - 发布菜谱（multipart/form-data，需认证）
   - 表单字段：`@RequestPart("request")` PublishRecipeRequestDTO + `@RequestParam("cover")` 封面 + `@RequestParam("images")` 图片列表
2. `GET /api/recipe/list` - 菜谱列表（@Public，分页 + 筛选）
   - 参数：category, difficulty, taste, keyword, page, size
3. `GET /api/recipe/{id}` - 菜谱详情（@Public，登录则回传点赞/收藏状态）

**Community 模块** (`/api/community`):

所有写操作和互动接口需要 `Authorization: Bearer <token>` 请求头。

1. `POST /api/community/recipe/{id}/like` - 点赞菜谱（需认证）
2. `DELETE /api/community/recipe/{id}/like` - 取消点赞（需认证）
3. `POST /api/community/recipe/{id}/collect` - 收藏菜谱（需认证）
4. `DELETE /api/community/recipe/{id}/collect` - 取消收藏（需认证）
5. `GET /api/community/recipe/{id}/comment` - 评论列表（@Public）
6. `POST /api/community/recipe/{id}/comment` - 发表评论（需认证）
7. `DELETE /api/community/comment/{id}` - 删除评论（需认证，仅本人）
8. `POST /api/community/comment/{id}/like` - 点赞评论（需认证）
9. `DELETE /api/community/comment/{id}/like` - 取消点赞评论（需认证）

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

**User 模块** (`/api/user`):

1. `GET /api/user/info` - 获取个人资料（需认证，含偏好+统计+手机脱敏）
2. `POST /api/user/info` - 更新个人资料（需认证，昵称/签名/背景/性别/偏好）
3. `POST /api/user/avatar` - 上传头像（需认证，multipart）
4. `POST /api/user/background` - 上传背景图（需认证，multipart）

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
| 2001 | 图片格式不支持 |
| 2002 | 图片大小超出限制 |
| 2003 | 图片包含违规内容 |
| 2004 | 图片审核服务异常 |

## 配置说明

三个配置文件按环境分离：

- **`application.yml`** — 共用配置（应用名、端口、JWT 过期时长 5min/30d、文件上传大小限制、Actuator 端点、MyBatis 驼峰映射）
- **`application-dev.yml`** — 开发环境（本地 MySQL + HikariCP 连接池、Redis 超时、SQL 日志打印、项目包 DEBUG 日志）
- **`application-prod.yml`** — 生产环境（数据库/微信/JWT 均通过 `${ENV_VAR}` 注入、HikariCP 连接池(20)、日志文件写入 `/var/log/flavor-creator`）
- **`logback-spring.xml`** — 日志配置（控制台输出 + 按日滚动文件 + ERROR 单独文件，保留 30 天）

## 测试

```bash
# 所有测试（146 个用例）
./mvnw test

# 单个测试类
./mvnw test -Dtest=UserServiceImplTest
```

### 测试分类

| 类型 | 注解 | 依赖 | 示例 |
|------|------|------|------|
| Mapper 集成 | `@SpringBootTest` + `@Transactional` | 真实 DB（自动回滚） | `UserMapperTest` |
| Service 单元 | `@ExtendWith(MockitoExtension.class)` | `@Mock` Mapper | `UserServiceImplTest` |
| Controller 单元 | `@ExtendWith(MockitoExtension.class)` | `@Mock` Service | `UserControllerTest` |
| 工具类纯单元 | 无注解 | 手动 new | `JwtTokenUtilTest` |

### 测试命名约定

- 类名：`{被测类}Test`
- 方法：`test{方法名}_{场景}` 如 `testLogin_ExistingUser`
- `@DisplayName("中文描述")`

## 注意事项

- 数据库 `flcr` 需要手动创建，所有建表脚本在 `script/sql/`
- Service 单测用纯 Mockito，不依赖数据库/Redis
- 图片上传经三步校验：`validate(type+size)` → `store(上传)` → `moderate(内容审核)`
- 图片审核 dev 环境跳过（`NoOpModerationServiceImpl`），cloud/prod 启用 COS CI（`CosModerationServiceImpl`）
- 认证通过 `AuthInterceptor` + `@Public` 注解实现，前端需传 `Authorization: Bearer <token>`
- `@MapperScan` 仅在 `BackendApplication` 上定义（`"flcr.backend.*.mapper"`），Mapper 接口无需 `@Mapper` 注解
- Controller 入参应加 `@Valid` 注解启用 DTO 字段校验，失败由 `GlobalExceptionHandler` 统一返回 400
- Controller 不手动 try-catch，依赖 `GlobalExceptionHandler` 统一处理
- Service 层通过 `UserContext.getUserId()` 获取当前用户，不在方法签名中传 userId