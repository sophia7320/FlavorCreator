# common/ — 公共基础设施

**模块**: `flcr.backend.common` | 跨模块共享的基础设施，不包含业务逻辑。

## STRUCTURE

```
common/
├── aop/            # 拦截器（AuthInterceptor, AdminAuthInterceptor）+ 切面（LoggingAspect）+ @Public
├── config/         # Spring 配置类（WebMvcConfig, CorsConfig, JacksonConfig 等 10 个）
├── constants/      # ResultCode + TargetTypeConstants + SourceConstants + GenderConstants
├── context/        # UserContext（ThreadLocal 用户上下文）
├── exception/      # BusinessException + GlobalExceptionHandler
├── response/       # Response<T> 统一响应
├── service/        # 通用服务接口 + impl/ 多套实现
└── util/           # 工具类（JwtTokenUtil, PasswordUtil）
```

## WHERE TO LOOK

| 需求 | 文件 | 说明 |
|------|------|------|
| 添加公开接口 | `aop/Public.java` | `@Public` 注解标记无需认证的接口 |
| 注册拦截器 | `config/WebMvcConfig.java` | user 拦截器 order=1，admin order=0 |
| 添加错误码 | `constants/ResultCode.java` | 静态 int 常量 |
| 提取枚举常量 | `constants/TargetTypeConstants.java` | RECIPE=1 / COMMENT=2 |
| 提取枚举常量 | `constants/SourceConstants.java` | SYSTEM=1 / USER=2 / AI=3 |
| 提取枚举常量 | `constants/GenderConstants.java` | UNKNOWN=0 / MALE=1 / FEMALE=2 |
| 抛业务异常 | `exception/BusinessException.java` | `BusinessException(code, message)` |
| 统一异常处理 | `exception/GlobalExceptionHandler.java` | 6 种异常处理器 |
| 文件存储 | `service/FileStorageService.java` | 3 套实现: Local（本地）/ COS（腾讯云）/ OSS |
| 图片审核 | `service/ImageModerationService.java` | 2 套实现: NoOp（dev,跳过）/ COS（生产,内容审核） |
| Refresh Token | `service/RefreshTokenService.java` | 2 套实现: InMemory（默认, ConcurrentHashMap + 定时清理）/ Redis（需显式配置 `flcr.token.store=redis`） |
| JWT 生成/验证 | `util/JwtTokenUtil.java` | 用户 Token（admin 用 AdminAuthServiceImpl） |
| 密码加密 | `util/PasswordUtil.java` | BCrypt + 明文迁移兼容 |

## CONVENTIONS

- **多实现切换**: 通过 `@ConditionalOnProperty`（`flcr.storage.type`, `flcr.moderation.enabled`, `flcr.token.store`）
- **dev 默认值**: `matchIfMissing = true` — dev 环境无需额外配置
- **ObjectMapper**: 由 `JacksonConfig` 创建 Bean，禁用 `WRITE_DATES_AS_TIMESTAMPS`
- **@MapperScan**: 全局在 `BackendApplication` — `"flcr.backend.*.mapper"`
- **ThreadLocal**: `UserContext` 在 `AuthInterceptor.afterCompletion()` 自动清理

## ANTI-PATTERNS

- **不要直接抛 RuntimeException** → 必须用 `BusinessException`
- **不要硬编码错误码数字** → 使用 `ResultCode` 常量
- **不要在拦截器以外调用 `UserContext`** → 只在 `AuthInterceptor` 中 set/clear
- **不要修改 `Response<T>` 的结构** → 前端依赖 `{code, message, data}`
- **JWT 密钥不要用默认值部署生产** → 通过环境变量覆盖 `jwt.secret` 和 `admin.jwt.secret`
