# 公共组件模块设计文档

**日期**: 2026-04-27  
**模块**: `flcr.backend.common`

---

## 1. 模块结构

```
flcr.backend.common/
├── aop/
│   ├── AuthAspect.java            Token 认证切面 (@Order(1))
│   ├── LoggingAspect.java         日志切面
│   └── RequireAuth.java           认证注解
├── constants/ResultCode.java      错误码常量
├── context/UserContext.java       ThreadLocal 用户上下文
├── exception/
│   ├── BusinessException.java     业务异常 (code, message)
│   └── GlobalExceptionHandler.java  全局异常处理
├── response/Response.java         统一响应 <T>
├── service/
│   ├── SmsService.java            短信接口
│   └── impl/SmsServiceImpl.java   短信实现（Redis 存储验证码）
├── config/                        微信/Redis 等配置
└── util/JwtTokenUtil.java         JWT 工具（生成、验证、解析）
```

---

## 2. 核心组件

### 2.1 AuthAspect
- 拦截 `@RequireAuth` 方法
- 从 `Authorization: Bearer <token>` 提取 JWT
- `required=true` 时 token 为空则抛 `BusinessException`
- `finally` 块中执行 `UserContext.clear()`

### 2.2 GlobalExceptionHandler
- `BusinessException` → `Response.error(code, message)`
- `Exception` → `Response.error(ResultCode.SYSTEM_ERROR, "服务器内部错误")`

### 2.3 统一响应 Response
```java
Response.success(data);
Response.success("msg", data);
Response.error(ResultCode.XXX, "msg");
```

### 2.4 UserContext
```java
UserContext.setUserId(Long);
UserContext.getUserId();  // 返回 Long，可能为 null
UserContext.clear();      // 在 AuthAspect finally 中调用
```

### 2.5 ResultCode
| 码 | 含义 |
|----|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 用户不存在 |
| 402 | 用户已存在 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 系统错误 |
| 1001 | 微信 code 无效 |
| 1002 | 微信接口调用失败 |
| 1003 | 手机号获取失败 |

---

## 3. 约定

- `BusinessException` 是 Service 层唯一允许抛出的异常
- Controller 层不手动 catch 异常，由 `GlobalExceptionHandler` 统一处理
- 错误码使用 `ResultCode` 常量，不硬编码数字
- 跨模块数据访问注入目标 Mapper，不注入 Service
