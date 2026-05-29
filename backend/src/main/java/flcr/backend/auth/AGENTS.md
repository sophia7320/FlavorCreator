# auth/ — 认证与用户实体模块

**模块**: `flcr.backend.auth` | 微信小程序登录、JWT 颁发、RefreshToken 管理。
此模块同时持有 `User` 实体，被 `user/` 模块依赖（`user/` 无独立 entity/mapper）。

## STRUCTURE

```
auth/
├── controller/     # UserController（2 端点: login-wx / refresh）
├── DTO/request/    # LoginRequestDTO, RefreshTokenRequestDTO, LogoutRequestDTO, PhoneBindRequestDTO
├── DTO/response/   # LoginResponseDTO（token + refreshToken + user）
├── entity/         # User（被 user/ 模块共用）
├── mapper/         # UserMapper
└── service/
    ├── UserService（接口）
    └── impl/UserServiceImpl（登录/JWT/刷新令牌）
```

## WHERE TO LOOK

| 需求 | 文件 | 说明 |
|------|------|------|
| 微信一键登录 | `controller/UserController.java` | `POST /api/auth/login-wx` |
| JWT 生成/验证 | `service/impl/UserServiceImpl.java` → `JwtTokenUtil` | Access Token 5min / Refresh Token 30d |
| RefreshToken 存储 | `common/service/RefreshTokenService.java` | Redis（默认）或 InMemory（cloud 环境） |
| 用户实体 | `entity/User.java` | 被 auth/ 和 user/ 共用 |
| 退出登录 | `controller/UserController.java` | `POST /api/auth/logout`，校验 RT 归属后删除 |

## CONVENTIONS

- **登录流程**: `wx.login()` 获取 code → 后端换取 openid → 查库/注册 → 生成 JWT + RefreshToken
- **JWT Claim**: `userId`(Long) + `openid`(String)
- **RefreshToken**: UUID 作为 Redis Key，`rt:{uuid}` → `{userId, openid}`，TTL 30 天
- **User 实体共享**: `user/` 模块无 entity/mapper，直接复用 `auth.entity.User`

## ANTI-PATTERNS

- **User 实体跨模块耦合**: `user/` 模块依赖 `auth/` 的 User，应提取公共 `user` 模块或合并 auth+user
- **手机号绑定预留**: `PhoneBindRequestDTO` 和 `/api/auth/phone/bind` 返回"功能开发中"
- **JWT 默认密钥**: `FlavorCreatorSecretKey2026` 为 dev 默认值，生产必须覆盖 `jwt.secret`
