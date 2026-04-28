# 认证模块设计文档

**日期**: 2026-04-27  
**模块**: `flcr.backend.auth`  
**依据**: 《创味机 API 接口文档》第二节"微信登录模块" + 第三节"用户信息模块"

---

## 1. 数据库表

### `user` 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO | 主键 |
| openid | VARCHAR(64) | 微信 openid，唯一 |
| unionid | VARCHAR(64) | 微信 unionid |
| nickname | VARCHAR(64) | 昵称 |
| avatar | VARCHAR(255) | 头像 |
| background | VARCHAR(255) | 背景图 |
| signature | VARCHAR(128) | 签名 |
| background | VARCHAR(255) | 背景图 |
| preferences | JSON | 偏好设置（taste/dietary/cookTime/difficulty） |
| gender | TINYINT | 0-未知 1-男 2-女 |
| phone_number | VARCHAR(20) | 手机号 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

---

## 2. 模块结构

```
flcr.backend.auth/
├── entity/User.java
├── mapper/UserMapper.java          extends BaseMapper<User>，无 @Mapper
├── DTO/
│   ├── request/
│   │   ├── LoginRequestDTO.java
│   │   └── RefreshTokenRequestDTO.java
│   └── response/
│       └── LoginResponseDTO.java   (含内部类 UserInfo)
├── service/
│   ├── UserService.java
│   └── impl/UserServiceImpl.java   extends ServiceImpl，@RequiredArgsConstructor
└── controller/
    └── UserController.java          /api/auth，@Slf4j
```

---

## 3. API

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login-wx` | 无 | 微信一键登录，code→openid→查/建用户→发 Token |
| POST | `/api/auth/refresh` | 无 | 刷新 Token |
| POST | `/api/auth/logout` | 无 | 退出登录（当前仅为前端清 Token） |

---

## 4. 关键设计决策

- **WxErrorException 处理**：在 Service 层捕获并转为 `BusinessException`，不向上抛
- **注解**：`@Slf4j` → `@Service` → `@RequiredArgsConstructor`
- **异常处理**：Controller 无 try-catch，依赖 `GlobalExceptionHandler`
- **待办**：logout 无服务端 Token 失效机制
