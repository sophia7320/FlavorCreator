# Redis Token 黑名单登录态管理 — 设计文档

**日期**: 2026-04-27  
**目标**: 解决 logout 无服务端 Token 失效机制的问题

---

## 1. 方案选择：黑名单模式

| 方案 | 原理 | 优缺点 |
|------|------|--------|
| **黑名单** ✅ | 退出时存 Redis，TTL=剩余时长 | 简单，每次请求额外一次 Redis 查询 |
| 白名单 | 登录时存 Redis，查询是否存在 | 每个请求都查，且需管理登录态生命周期 |

选择黑名单：退出是低频操作，正常请求只需一次 `EXISTS` 查询，性能开销极小。

---

## 2. Redis 设计

```
Key:   token:blacklist:{jti}   (JWT 的 jti claim，UUID 36 字节)
Value: "1"
TTL:   token 剩余有效毫秒数（过期 ≤0 则不入库）
```

自动清理：Redis TTL 到期自动删除，无需定时任务。

---

## 3. 数据流

```
登录     → 生成 access_token(2h) + refresh_token(7d)
请求     → AuthAspect 提取 token → 查 Redis 黑名单 → 校验 JWT
退出     → AuthAspect 将 token 存入 UserContext → Controller 取出 → blacklist
刷新     → 旧 refresh_token 加入黑名单 → 签发新 token
```

---

## 4. 模块结构

### 新建

| 文件 | 位置 |
|------|------|
| `TokenBlacklistService.java` | `common/service/` |
| `TokenBlacklistServiceImpl.java` | `common/service/impl/` |

### 修改

| 文件 | 改动 |
|------|------|
| `UserContext.java` | 新增 `jti` ThreadLocal + `getJti()/setJti()/clear()` |
| `JwtTokenUtil.java` | ①`generateToken` 内嵌 `jti`(UUID) ②新增 `getJtiFromToken()` ③新增 `getRemainingTime()` |
| `AuthAspect.java` | ① `UserContext.setJti(jti)` ② 黑名单检查用 jti |
| `UserController.java` | `logout()` 从 UserContext 取 token → blacklist |
| `UserServiceImpl.java` | `refreshToken()` 校验通过后 blacklist 旧 refresh_token |

---

## 5. 异常处理

- **Redis 不可用**：`isBlacklisted()` 内部 catch 异常 → 返回 false 放行（可用性优先）
- **已过期 Token**：`getRemainingTime() ≤ 0` → 不写入 Redis
- **黑名单命中**：`AuthAspect` 抛 `BusinessException(ResultCode.USER_NOT_EXIST, "Token已失效，请重新登录")`
