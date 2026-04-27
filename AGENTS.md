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
- 需要认证的 Controller 方法标注 `@RequireAuth`（默认 required=true）
- Service 层通过 `UserContext.getUserId()` 获取当前用户，**不在方法签名中传 userId**
- `AuthAspect` 在请求结束后自动 `UserContext.clear()`

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
    @RequireAuth                  // 需要认证
    public Response<XxxDTO> action(@Valid @RequestBody XxxRequestDTO request) {
        return Response.success(xxxService.action(request));
    }

    @GetMapping("/public")
    @RequireAuth(required = false) // 可选认证（游客可访问）
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
| Token 过期 | 访问 2h / 刷新 7d |
| JWT Claim | userId(L), openid(S) |
| Actuator 端点 | health, info |

## 新模块开发清单

1. 创建 `flcr.backend.{模块名}` 包
2. 创建 `entity/` → Entity 类（按上方模板）
3. 创建 `mapper/` → 继承 `BaseMapper<Entity>`
4. 创建 `service/` → 接口 + `impl/` 实现类
5. 创建 `DTO/request/` + `DTO/response/` → 请求/响应 DTO
6. 创建 `controller/` → `@RestController` + `@RequestMapping("/api/模块名")`
7. 需要认证的方法加 `@RequireAuth`
8. 需要参数校验的 Controller 入参加 `@Valid` + 在 DTO 字段上加校验注解
9. 在 `src/test/` 对应包下写测试（集成测试用 `@SpringBootTest` + `@Transactional` 回滚）

## 已知待办

- 文件上传为占位符实现（`/uploads/cover.jpg`），需对接 OSS
- `CommunityServiceImpl.likeComment()` / `unlikeComment()` 为 TODO 空实现
- 点赞/收藏计数使用 read-modify-write，存在并发竞态风险
- `RecipeListRequestDTO.taste` 字段已定义但在 Service 中未使用
- `recipe` 模块仅有 Entity + Mapper，Controller/Service 待开发
- `admin` 模块待实现
- `JacksonConfig` 手动创建 `ObjectMapper` Bean 作为 Spring Boot 4 `tools.jackson` 命名空间的桥接，若迁移到 Jackson 3 后可移除
