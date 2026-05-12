# AGENTS.md

此文件为 AI 编码提供架构思路和编码偏好指引。项目技术栈见 CLAUDE.md。

## 架构原则

### 分层不可跨越
```
Controller → Service(接口) → Service/impl → Mapper
```
- Controller 只做参数接收和调用 Service，不直接调用 Mapper
- Service 层不返回 Controller 层的 Request DTO
- 跨模块引用时直接注入目标模块的 **Mapper**，不注入 Service（避免循环依赖）

### 认证模型
- `WebMvcConfig` 注册 `AuthInterceptor` 拦截所有 `/api/**` 请求
- **无需认证**的 Controller 方法标注 `@Public`（游客可访问，有 Token 则解析写入 UserContext）
- **无 `@Public` 注解** = 需要认证，无 Token 则返回 401
- Service 层通过 `UserContext.getUserId()` 获取当前用户，**不在方法签名中传 userId**
- `AuthInterceptor.afterCompletion()` 自动 `UserContext.clear()`

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
| Token 过期 | 访问 5min / 刷新 30d |
| JWT Claim | userId(L), openid(S) |
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
- `admin` 模块待实现
- 手机号绑定 (`/api/auth/phone-wx`) 待实现
- 个人中心 (收藏/发布/历史/点赞) 的专用 API 端点待实现
- 图片审核启用后需在云环境配置 COS CI 相关环境变量（`COS_SECRET_ID` 等）

## 已知限制

- 菜谱批量图片上传时，若第 N 张审核失败，前 N-1 张已上传的 COS 文件会成为孤儿（DB事务回滚但文件无法回滚）。当前可接受，长期应将 upload 延后到全部图片审核通过后统一执行。
- `CosModerationServiceImpl.moderate()` 的 COSClient 每次新建，与 `CosFileStorageServiceImpl.store()` 各自独立创建，存在重复连接开销。

## 图片上传与审核

- 所有图片上传入口（`/api/user/avatar`、`/api/user/background`、`/api/recipe`）均调用 `ImageModerationService` 三步校验
- 三步流程：`validate(file, scene)` → `FileStorageService.store()` → `moderate(url, scene)`
- dev 环境：`NoOpModerationServiceImpl`（仅类型+大小校验，跳过内容审核）
- cloud/prod 环境：`CosModerationServiceImpl`（类型+大小校验 + 腾讯云 COS CI 内容审核）
- 审核不通过自动删除 COS 对象后抛 `BusinessException`
- 错误码：2001(格式) / 2002(大小) / 2003(内容违规) / 2004(审核异常)
- 配置前缀：`flcr.moderation`（`enabled`、`allowed-types`、`max-size.{scene}`）
