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
├── ingredient/ # 食材模块 - 待实现
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
- 使用 `@RestController` + `@RequestMapping("/api/模块")`
- 统一返回 `Response<T>` 泛型类（位于 `common.response` 包）
- 构造器注入依赖（Lombok `@RequiredArgsConstructor`）
- 需要认证的方法标记 `@RequireAuth`，通过 `UserContext.getUserId()` 获取当前用户 ID
- 请求体通过 DTO 接收，multipart 表单自动绑定到 DTO

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

### 核心组件

**WxMaConfiguration** (`common.config`):
- 配置微信小程序 SDK
- 支持微信云托管环境（从文件路径读取 access_token）
- 提供 `WxMaService` Bean

**JacksonConfig** (`common.config`):
- 配置 `ObjectMapper` Bean，禁用 `WRITE_DATES_AS_TIMESTAMPS`
- 用于 Entity 中 JSON 字段的序列化/反序列化

**MyBatisConfig** (`common.config`):
- MyBatis 配置类（预留分页插件等配置入口）
- Mapper 扫描由 `BackendApplication` 上的 `@MapperScan` 负责

**LoggingAspect** (`common.aop`):
- AOP 环绕通知，记录所有 Controller 和 Service 方法调用
- 输出：类名、方法名、入参、返回值、执行耗时
- 异常时输出错误信息和耗时

**AuthAspect** (`common.aop`, @Order(1)):
- 拦截 `@RequireAuth` 标记的方法，从 `Authorization: Bearer <token>` 中提取 JWT
- 验证 Token 有效后将 userId 写入 `UserContext`（ThreadLocal）
- `@RequireAuth(required = false)` 时 Token 可选，有则解析，无则放行

**RequireAuth** (`common.aop`):
- 标记需要 Token 认证的 Controller 方法
- `required = true`（默认）：无有效 Token 抛 BusinessException(401)
- `required = false`：游客可访问，有 Token 则解析 userId

**UserContext** (`common.context`):
- ThreadLocal 持有当前请求的 userId
- Controller 通过 `UserContext.getUserId()` 获取
- AuthAspect 在请求结束后自动 clean

**BusinessException** (`common.exception`):
- 业务异常类，携带 `code` 状态码
- 构造器：`BusinessException(code, message)` 或 `BusinessException(message)`（默认 500）
- **Service 层必须使用 `BusinessException`，禁止直接 `throw new RuntimeException()`**，否则 `GlobalExceptionHandler` 无法返回业务错误码

**GlobalExceptionHandler** (`common.exception`):
- `@RestControllerAdvice` 全局异常处理器
- 捕获 `BusinessException` 返回对应 code 和 message
- 捕获 `Exception` 返回 500 通用错误

**JwtTokenUtil** (`common.util`):
- `generateToken(userId, openid)` - 生成访问令牌（2 小时）
- `generateRefreshToken(userId, openid)` - 生成刷新令牌（7 天）
- `validateToken(token)` - 验证令牌
- `getUserIdFromToken(token)` - 解析用户 ID
- `getOpenidFromToken(token)` - 解析 OpenID

**Response** (`common.response`):
- 统一响应格式：`{code, message, data}`
- 静态工厂方法：`success()`, `success(T)`, `success(String, T)`, `error(code, msg)`

### API 接口

**Auth 模块** (`/api/auth`):

1. `POST /api/auth/login-wx` - 微信一键登录
   - 入参：`LoginDTO` (code, userInfo)
   - 出参：`Response<LoginResponseDTO>` (token, refreshToken, user)

2. `POST /api/auth/refresh` - 刷新 Token
   - 入参：`RefreshTokenDTO` (refreshToken)
   - 出参：`Response<LoginResponseDTO>`

3. `POST /api/auth/logout` - 退出登录

**Community 模块** (`/api/community`):

所有写操作和互动接口需要 `Authorization: Bearer <token>` 请求头。

1. `POST /api/community/recipe` - 发布菜谱（multipart/form-data，需认证）
   - 表单字段：name, ingredients, steps, cover(文件), images(文件), tags, category, tips, cookTime, difficulty, calories

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

### 错误码定义 (`common.constants.ResultCode`)

| 码 | 说明 |
|----|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 用户不存在/Token 过期 |
| 403 | 权限不足 |
| 500 | 系统错误 |
| 1001 | 微信 code 无效 |
| 1002 | 微信接口调用失败 |
| 1003 | 手机号获取失败 |

## 配置说明

三个配置文件按环境分离：

- **`application.yml`** — 共用配置（应用名、端口、JWT 过期时长、MyBatis 驼峰映射）
- **`application-dev.yml`** — 开发环境（本地 MySQL、SQL 日志打印、dev 凭证）
- **`application-prod.yml`** — 生产环境（数据库/微信/JWT 均通过 `${ENV_VAR}` 注入，SQL 日志关闭）

## 测试

```bash
# 所有测试（UserMapper 9 + ResultCode 3 + BusinessException 4 + Response 7 + JwtTokenUtil 13 = 36 个）
./mvnw test

# 单个测试类
./mvnw test -Dtest=JwtTokenUtilTest
./mvnw test -Dtest=ResponseTest
./mvnw test -Dtest=BusinessExceptionTest
./mvnw test -Dtest=ResultCodeTest
./mvnw test -Dtest=UserMapperTest
```

## 注意事项

- 数据库 `flcr` 需要手动创建，建表脚本在 `script/sql/` 目录
- Community 模块中点赞评论（`likeComment`/`unlikeComment`）为 TODO 状态，待完善
- 菜谱的图片上传逻辑为占位符，实际文件存储待实现
- Admin 和 Ingredient 模块尚未实现
- 认证通过 `@RequireAuth` + `AuthAspect` 实现，前端需传 `Authorization: Bearer <token>`
- `@MapperScan` 仅在 `BackendApplication` 上定义（`"flcr.backend.*.mapper"`），Mapper 接口无需 `@Mapper` 注解