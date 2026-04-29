# Token 机制重构设计

## 1. 背景与目标

现有 JWT 方案存在**静默 Bug**：`TokenBlacklistServiceImpl.blacklist()` 传入的是 jti（UUID 字符串），但内部调用 `jwtTokenUtil.getRemainingTime(token)` 会尝试将 UUID 字符串作为 JWT decode，导致 `remainingMs` 恒为 0，黑名单永远不会写入 Redis，**登出后 token 实际仍可用**。

同时每次 API 请求都需要一次 Redis 黑名单查询（`isBlacklisted`），有性能开销。

## 2. 设计原则

- **高频操作零 IO**：Access Token 每请求都用，纯 JWT 不查 Redis
- **低频操作重安全**：Refresh Token 每次刷新（5 分钟一次）才查一次 Redis
- **Rotation 防护重放**：旧 RT 刷新后立即失效，被盗也无法重放
- **登出即时失效**：DEL RT 后无法换新 AT

## 3. 新 Token 架构

### Access Token
| 项目 | 内容 |
|------|------|
| 格式 | JWT（HMAC256） |
| 有效期 | **5 分钟** |
| 存储 | 客户端内存，不存本地 |
| 失效方式 | 自然过期（无黑名单/白名单） |
| Claims | userId(Long), openid(String), exp(Long), iat(Long) |

### Refresh Token
| 项目 | 内容 |
|------|------|
| 格式 | **UUID v4**（36 字符，不含 JWT） |
| 有效期 | **30 天** |
| 存储 | 客户端本地（持久化） |
| 状态管理 | Redis，`Key: rt:{uuid}`, `Val: {"userId":123,"openid":"xxx"}` |

## 4. Redis 数据结构

```
Key:   rt:{uuid}
Val:   JSON: {"userId": 123, "openid": "oxxx"}
TTL:   2592000 秒（30 天）
```

## 5. 各操作流程

### 登录 `POST /api/auth/login-wx`
1. 微信 code2Session → openid
2. 查/建 User → userId
3. 生成 AT：`jwtTokenUtil.generateToken(userId, openid)`（5min TTL）
4. 生成 RT：UUID v4
5. `redisTemplate.opsForValue().set("rt:" + uuid, JSON{userId, openid}, 30d)`
6. 返回 `{token, refreshToken, expiresIn:300, user}`

### 刷新 `POST /api/auth/refresh`
**前置**：AT 已过期，客户端用 refreshToken 换新
1. `redisTemplate.opsForValue().get("rt:" + oldUuid)` → null 则拒绝
2. 解析旧 RT 中的 userId（从 Redis 值中取）
3. `redisTemplate.delete("rt:" + oldUuid)` — 旧 RT 立即作废（Rotation）
4. 生成新 AT + 新 RT(UUID)
5. `redisTemplate.opsForValue().set("rt:" + newUuid, JSON{userId, openid}, 30d)`
6. 返回新 `{token, refreshToken, expiresIn:300}`

### 登出 `POST /api/auth/logout`
**前置**：需传 refreshToken（在 body 中）
1. AuthAspect 验 AT 签名+过期
2. `redisTemplate.delete("rt:" + rtUuid)`
3. 返回成功

### 请求认证（AuthAspect）
1. 从 Header 提取 Bearer token
2. `jwtTokenUtil.validateToken(token)` — 签名 + 过期
3. 解析 userId → `UserContext.setUserId(userId)`
4. **不再查 Redis**，零 IO
5. finally: `UserContext.clear()`

## 6. 攻击场景

| 场景 | 防护效果 |
|------|---------|
| AT 被盗 | 5 分钟窗口，攻击者最多用 5 分钟 |
| RT 被盗后重放 | 旧 RT 已被 DEL，重放拒绝 |
| RT 被盗后换 AT | RT 已 DEL，无法换 AT |
| 攻击者伪造 RT UUID | Redis GET 不存在，拒绝 |
| 正常用户登出后 | RT DEL，无法换 AT，AT 最多再活 5 分钟 |

## 7. 代码变更

### 删除
- `TokenBlacklistService.java`（接口）
- `TokenBlacklistServiceImpl.java`（实现）
- `TokenBlacklistServiceImplTest.java`

### 新增
- `RefreshTokenService.java`（接口）
- `RefreshTokenServiceImpl.java`（实现）
- `RefreshTokenServiceImplTest.java`

### 修改
- `JwtTokenUtil.java`：移除 `getJtiFromToken`、`getRemainingTime`、`isTokenExpired`（不需要）
- `AuthAspect.java`：移除 `isBlacklisted` 调用和 jti 相关逻辑
- `UserContext.java`：移除 `JTI` ThreadLocal，只保留 `USER_ID`
- `UserServiceImpl.java`：login/refresh/logout 接入 RefreshTokenService
- `UserController.java`：logout 接口接收 `refreshToken` 参数（从 body）
- `application.yml`：`jwt.expiration` 改为 `300000`（5min），新增 `jwt.refresh-expiration: 2592000000`（30d）
- `application-dev.yml` / `application-prod.yml` 同上配置同步
- `RequireAuth.java`：不变

### 测试更新
- `AuthAspectTest.java`：移除 jti 黑名单相关测试用例
- `JwtTokenUtilTest.java`：移除 getJtiFromToken 等方法的测试
- `UserServiceImplTest.java`：更新 login/refresh/logout mock 逻辑
- 新增 `RefreshTokenServiceImplTest.java`

## 8. 接口变化

| 接口 | 变化 |
|------|------|
| `POST /api/auth/login-wx` | 不变 |
| `POST /api/auth/refresh` | 不变（已有 refreshToken 参数） |
| `POST /api/auth/logout` | **body 新增 refreshToken 字段**（之前没有） |

`LoginRequestDTO` / `RefreshTokenRequestDTO` 不变，`LogoutRequestDTO` 新增 `refreshToken` 字段。

## 9. 配置

```yaml
jwt:
  secret: ${JWT_SECRET:FlavorCreatorSecretKey2026}
  expiration: 300000          # 5 分钟（ms）
  refresh-expiration: 2592000000  # 30 天（ms）
```

## 10. 命名建议

| 旧命名 | 新命名 | 原因 |
|--------|--------|------|
| `TokenBlacklistService` | `RefreshTokenService` | 不再是黑名单，是 RT 管理服务 |
| `token:blacklist:{jti}` | `rt:{uuid}` | 不再是黑名单 Key，是 RT Key |
| `refreshToken` 参数 | 不变 | 保持一致 |

## 11. 多设备扩展预留

当前设计是每用户只有一个有效 RT（DEL 旧 → SET 新）。如需多设备，改法简单：

```
Key: rt:{userId}:{uuid}
Val: {"userId":123, "openid":"oxxx", "device":"iPhone 15"}
TTL: 30d
```

每个设备独立 RT，logout 时 DEL 特定 Key 即可。当前不需要，暂不实现。
