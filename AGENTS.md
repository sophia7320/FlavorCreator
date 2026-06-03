# AGENTS.md

**Generated:** 2026-05-26 | **Commit:** 81a937f | **Branch:** master

此文件为 AI 编码提供架构思路和编码偏好指引。项目技术栈见 CLAUDE.md。

## 工作范围

**本知识库仅覆盖后端（`backend/`）**。`miniprogram/`（微信小程序前端）不在 AI 编码范围内，请勿修改或为其生成文档。

## 模块索引

子目录 AGENTS.md 见各后端模块目录（`auth/`、`common/`、`admin/`、`community/`、`recipe/`、`ingredient/`、`user/`）。

## 快速查找

| 任务 | 位置 | 备注 |
|------|------|------|
| 添加 API 接口 | `backend/src/main/java/flcr/backend/{模块}/controller/` | `@RestController` + `@RequestMapping("/api/{模块}")` |
| 添加业务逻辑 | `backend/src/main/java/flcr/backend/{模块}/service/impl/` | 接口在 `service/`，实现在 `impl/` |
| 添加数据库操作 | `backend/src/main/java/flcr/backend/{模块}/mapper/` | 继承 `BaseMapper<Entity>` |
| 添加 Entity | `backend/src/main/java/flcr/backend/{模块}/entity/` | `@TableName` + `@TableId(type=AUTO)` |
| 添加 DTO | `backend/src/main/java/flcr/backend/{模块}/DTO/request/` 或 `response/` | 请求四合一之外的 `@NoArgsConstructor` |
| 认证拦截 | `common/aop/AuthInterceptor.java` | 拦截 `/api/**`，尊重 `@Public` 注解 |
| 管理员认证 | `common/aop/AdminAuthInterceptor.java` | 拦截 `/api/admin/**`，独立 JWT |
| 错误码 | `common/constants/ResultCode.java` | 所有业务错误码常量 |
| 统一响应 | `common/response/Response.java` | `Response.success(data)` / `Response.error(code, msg)` |
| 图片上传/审核（底层） | `common/service/ImageModerationService.java` | validate(格式+大小) → moderate(内容审核) |
| 图片上传（统一入口） | `common/service/ImageUploadService.java` | 封装 validate→store→moderate，失败自动清理 |
| 文件存储 | `common/service/FileStorageService.java` | Local/COS/OSS 三套实现 |
| 图片上传场景 | `common/constants/ImageScene.java` | AVATAR / BACKGROUND / RECIPE_COVER / RECIPE_IMAGE |
| 通用图片上传接口 | `common/controller/ImageController.java` | `POST /api/image/upload?scene=` |
| JWT Token | `common/util/JwtTokenUtil.java` | 用户 JWT（userId+openid） |
| 用户上下文 | `common/context/UserContext.java` | ThreadLocal，拦截器自动清理 |
| 全局异常处理 | `common/exception/GlobalExceptionHandler.java` | `@RestControllerAdvice` |
| 新模块开发 | 按下方清单逐步创建 | 参考 `AGENTS.md` 编码契约 

## 架构原则

### 分层不可跨越
```
Controller → Service(接口) → Service/impl → Mapper
```
- Controller 只做参数接收和调用 Service，不直接调用 Mapper
- Service 层不返回 Controller 层的 Request DTO
- 跨模块引用时直接注入目标模块的 **Mapper**，不注入 Service（避免循环依赖）

### 用户认证模型
- `WebMvcConfig` 注册 `AuthInterceptor` 拦截所有 `/api/**` 请求（用户端）
- **无需认证**的 Controller 方法标注 `@Public`（游客可访问，有 Token 则解析写入 UserContext）
- **无 `@Public` 注解** = 需要认证，无 Token 则返回 401
- Service 层通过 `UserContext.getUserId()` 获取当前用户，**不在方法签名中传 userId**
- `AuthInterceptor.afterCompletion()` 自动 `UserContext.clear()`

### 管理员认证模型
- `WebMvcConfig` 注册 `AdminAuthInterceptor` 拦截 `/api/admin/**` 请求
- 使用独立 JWT 密钥（`admin.jwt.secret`），**不与用户 Token 互通**
- 用户 Token 无法访问 Admin 接口（返回 BusinessException）
- 排除路径：`/api/admin/auth/login`（登录）、`/api/admin/auth/refresh`（刷新）
- AdminAuthInterceptor 从 token 中解析 `adminId` 写入 request attribute

### 错误处理
- Service 层只能抛 `BusinessException(code, message)`，禁止 `throw new RuntimeException()`
- 错误码使用 `ResultCode` 常量，不硬编码数字
- `GlobalExceptionHandler` 统一捕获并转换为 `Response<T>`，现处理：
  - `BusinessException` → 返回对应 code + message
  - `MethodArgumentNotValidException` → 400（`@Valid` 校验失败）
  - `ConstraintViolationException` → 400（方法级校验失败）
  - `HttpMessageNotReadableException` → 400（JSON 格式错误）
  - `BindException` → 400（表单绑定失败）
  - `Exception` → 500（兜底）

## 编码契约

### Entity 层
```java
@Data
@TableName(value = "`table_name`", autoResultMap = true)  // autoResultMap 支持 JSON 列
public class Xxx {
    @TableId(type = IdType.AUTO)
    private Long id;
    // JSON 字段全部使用 String 类型
    private String jsonField;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Mapper 层
```java
// 继承 BaseMapper，不写 SQL（除非复杂查询必须）
public interface XxxMapper extends BaseMapper<Xxx> {
}
// 不需要 @Mapper 注解，由 BackendApplication 上的 @MapperScan("flcr.backend.*.mapper") 扫描
```

### DTO 层
- **请求 DTO**：`@Data`，放在 `DTO/request/`，命名后缀 `XxxRequestDTO`
  - 内部静态类只用 `@Data` + `@NoArgsConstructor`（Jackson 反序列化需要）
- **响应 DTO**：`@Data` + `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`（四合一），放在 `DTO/response/`
  - 内部静态类同样四合一注解

### Service 层
```java
public interface XxxService { }
```
```java
@Service
@RequiredArgsConstructor     // 构造器注入所有依赖
public class XxxServiceImpl implements XxxService {
    // 需要跨模块数据时注入 Mapper，不注入 Service
    private final XxxMapper xxxMapper;
    private final ObjectMapper objectMapper;  // 处理 JSON 字段转换
}
```
- 写操作加 `@Transactional`
- JSON 字段读写：`objectMapper.writeValueAsString(obj)` / `objectMapper.readValue(str, TypeRef)`
- 内部临时结构体可用 Java `record`（如 `UserWithStatus`）

### Controller 层
```java
@RestController
@RequestMapping("/api/模块名")
@RequiredArgsConstructor
public class XxxController {
    private final XxxService xxxService;

    @PostMapping("/action")
    // 无 @Public 注解 = 需要认证
    public Response<XxxDTO> action(@Valid @RequestBody XxxRequestDTO request) {
        return Response.success(xxxService.action(request));
    }

    @GetMapping("/public")
    @Public                        // 游客可访问（有 Token 则解析写入 UserContext）
    public Response<XxxDTO> publicEndpoint() {
        Long userId = UserContext.getUserId(); // 可能为 null
        return Response.success(xxxService.query(userId));
    }
}
```
- 请求参数加 `@Valid` / `@Validated` 启用 DTO 字段校验
- 校验失败由 `GlobalExceptionHandler` 统一返回 400 错误

### 统一响应
```java
// 成功
Response.success(data);
Response.success("自定义消息", data);

// 失败
Response.error(ResultCode.PARAM_ERROR, "参数错误");
Response.error("服务器错误");  // 默认 code=500
```

## 约定速查表

| 字段 | 取值 | 含义 |
|------|------|------|
| `difficulty` | 1 / 2 / 3 | 简单 / 中等 / 困难 |
| `source` | 1 / 2 / 3 | 系统 / 用户 / AI |
| `targetType` | 1 / 2 | 菜谱 / 评论 |
| `gender` | 0 / 1 / 2 | 未知 / 男 / 女 |
| `status` | normal / expiring / expired | 食材状态（动态计算，不入库） |

| 配置 | 值 |
|------|-----|
| API 前缀 | `/api/{模块名}` |
| CORS | 允许 `/api/**` 所有来源跨域 |
| 时间格式 | `DateTimeFormatter.ISO_LOCAL_DATE_TIME` |
| 分页默认 | page=1, size=20 |
| 文件上传限制 | 单文件 10MB / 总请求 20MB |
| 用户 Token 过期 | 访问 5min / 刷新 30d |
| 管理员 Token 过期 | 访问 2h / 刷新 7d |
| 用户 JWT Claim | userId(L), openid(S) |
| 管理员 JWT Claim | adminId(L), username(S), role(S) |
| Actuator 端点 | health, info |

## 测试约定

### 分类
| 测试类型 | 注解 | 特点 |
|---------|------|------|
| Mapper 集成 | `@SpringBootTest` `@Transactional` `@ActiveProfiles("dev")` | 真实数据库，自动回滚 |
| Service 单元 | `@ExtendWith(MockitoExtension.class)` | `@Mock` Mapper，`@InjectMocks` Service |
| Controller 单元 | `@ExtendWith(MockitoExtension.class)` | `@Mock` Service，`@InjectMocks` Controller |
| 工具类纯单元 | 无 Spring 注解 | `new` 被测对象，`ReflectionTestUtils` 注入配置 |

### 命名
- 类名：`{被测类}Test`，方法：`test{方法}_{场景}`
- `@DisplayName("中文描述")`
- lambda 内抛异常用 `assertThrows`，mock 空调用用 `assertDoesNotThrow`

## 新模块开发清单

1. 创建 `flcr.backend.{模块名}` 包
2. 创建 `entity/` → Entity 类（按上方模板）
3. 创建 `mapper/` → 继承 `BaseMapper<Entity>`
4. 创建 `service/` → 接口 + `impl/` 实现类
5. 创建 `DTO/request/` + `DTO/response/` → 请求/响应 DTO
6. 创建 `controller/` → `@RestController` + `@RequestMapping("/api/模块名")`
7. 无需认证的公开方法加 `@Public`，其他方法默认需要认证
8. 需要参数校验的 Controller 入参加 `@Valid` + 在 DTO 字段上加校验注解
9. 在 `src/test/` 对应包下写测试（集成测试用 `@SpringBootTest` + `@Transactional` 回滚）

## 已知待办

- `RecipeListRequestDTO.taste` 字段已定义但在 Service 中未使用
- 图片审核启用后需在云环境配置 COS CI 相关环境变量（`COS_SECRET_ID` 等）
- Admin 登录密码当前为明文比较，应改为 BCrypt 加密存储
- `TestController` 包名 `flcr.backend.Test` 首字母大写不规范，建议移至 `controller/` 或删除

## 已知限制

- 菜谱 publishRecipe 中 cover 和 images 分属不同 scene，各调用 ImageUploadService.upload() 独立原子化。若 cover 上传成功但后续某张 image 失败，cover 文件会成为孤儿。跨 scene 非原子性可接受。
- `buildSingleCommentDTO()` N+1 查询：流中每层调 `userMapper.selectById`（不同于批量的 `buildCommentDTO`）

## 图片上传与审核

- **统一入口**：`ImageUploadService.upload(file, scene)` 封装三步流程，失败自动清理已存储文件
- **通用接口**：`POST /api/image/upload`（`file` + `scene`），需登录
- 业务端点（`/api/user/avatar`、`/api/user/background`、`/api/recipe`）内部委托给 `ImageUploadService`
- 三步流程：`ImageModerationService.validate(file, scene)` → `FileStorageService.store()` → `ImageModerationService.moderate(url, scene)`
- dev 环境：`NoOpModerationServiceImpl`（仅类型+大小校验，跳过内容审核）
- cloud/prod 环境：`CosModerationServiceImpl`（类型+大小校验 + 腾讯云 COS CI 内容审核）
- 审核不通过自动删除 COS 对象后抛 `BusinessException`
- 错误码：2001(格式) / 2002(大小) / 2003(内容违规) / 2004(审核异常) / 2005(无效场景)
- 配置前缀：`flcr.moderation`（`enabled`、`allowed-types`、`max-size.{scene}`）
