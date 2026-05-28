# admin/ — 管理员模块

**模块**: `flcr.backend.admin` | 管理员后台，使用独立 JWT 认证体系。

## STRUCTURE

```
admin/
├── controller/     # 5 个 Controller（Auth, Recipe, Comment, User, Stats）
├── DTO/request/    # 12 个请求 DTO
├── DTO/response/   # 5 个响应 DTO
├── entity/         # Admin 实体
├── mapper/         # AdminMapper
└── service/        # 4 个 Service 接口 + impl/
    ├── AdminAuthService — 管理员登录/JWT 验证（独立密钥 admin.jwt.secret）
    ├── AdminContentService — 菜谱+评论 CRUD + 状态管理
    ├── AdminUserService — 用户管理（列表/详情/状态变更）
    └── AdminStatsService — 数据统计总览
```

## WHERE TO LOOK

| 需求 | 文件 | 说明 |
|------|------|------|
| 管理员登录 | `controller/AdminAuthController.java` | `POST /api/admin/auth/login`，明文密码（见已知待办） |
| 管理员认证 | `service/impl/AdminAuthServiceImpl.java` | 独立 JWT，不互通用户 Token |
| 菜谱管理 CRUD | `controller/AdminRecipeController.java` → `AdminContentServiceImpl` | 含状态变更（上架/下架/审核） |
| 评论管理 CRUD | `controller/AdminCommentController.java` → `AdminContentServiceImpl` | 含状态变更（显示/隐藏） |
| 用户管理 | `controller/AdminUserController.java` → `AdminUserServiceImpl` | 启用/禁用状态切换 |
| 数据统计 | `controller/AdminStatsController.java` → `AdminStatsServiceImpl` | 用户+内容+审核统计 |
| 管理员实体 | `entity/Admin.java` | username/encryptedPassword/role/status |

## CONVENTIONS

- **Admin JWT Claim**: adminId(L), username(S), role(S)
- **Admin Token 过期**: 访问 2h / 刷新 7d
- **auth 排除路径**: `/api/admin/auth/login`、`/api/admin/auth/refresh`
- **Admin ID 存储**: `request.setAttribute("adminId", adminId)` — 不在 UserContext
- **Controller**: `@RequestMapping("/api/admin/{资源}")`, 使用 `@Valid` + `@RequestBody`

## ANTI-PATTERNS

- **Admin 密码当前为明文比对** — `PasswordUtil.match()` 有明文 fallback，应全量迁移为 BCrypt
- **`AdminAuthServiceImpl` 刷新令牌未实现** — `refresh()` 抛 "刷新令牌功能开发中"
- **Admin JWT 默认密钥硬编码** — `@Value("${admin.jwt.secret:AdminSecretKey2026}")`，生产必须覆盖
