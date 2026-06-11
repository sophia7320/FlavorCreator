# ingredient/ — 食材与调味品模块

**模块**: `flcr.backend.ingredient` | 食材 CRUD、临期提醒、常用食材库、调味品（复用食材逻辑）。

## STRUCTURE

```
ingredient/
├── cache/          # IngredientHeapCache（接口）+ InMemoryIngredientHeapCache + RedisIngredientHeapCache
├── constants/      # IngredientStatus 枚举
├── controller/     # IngredientController + CondimentController（复用 IngredientService）
├── DTO/request/    # IngredientAddRequestDTO, IngredientUpdateRequestDTO, IngredientBatchAddRequestDTO, IngredientListRequestDTO
├── DTO/response/   # IngredientResponseDTO, IngredientListResponseDTO, CommonIngredientResponseDTO, ExpiringNoticeResponseDTO
├── entity/         # Ingredient, CommonIngredient
├── mapper/         # IngredientMapper, CommonIngredientMapper
└── service/
    ├── IngredientService（接口）
    ├── IngredientSchedulerService（定时任务）
    └── impl/IngredientServiceImpl
```

## WHERE TO LOOK

| 需求 | 文件 | 说明 |
|------|------|------|
| 添加/更新/删除食材 | `controller/IngredientController.java` → `IngredientServiceImpl` | 写入后同步堆缓存 |
| 批量添加食材 | `controller/IngredientController.java` | `POST /api/ingredient/batch` |
| 临期/过期提醒 | `controller/IngredientController.java` | `GET /api/ingredient/expiring-notice`，从堆缓存读取 |
| 确认已读 | `controller/IngredientController.java` | `POST /api/ingredient/{id}/read` + `POST /api/ingredient/read` |
| 常用食材分类 | `controller/IngredientController.java` | `GET /api/ingredient/common`，游客可访问 |
| 调味品接口 | `controller/CondimentController.java` | 复用 IngredientService，category 固定为"调味品" |
| 食材实体 | `entity/Ingredient.java` | 含 expire_date、readed；status 动态计算（不入库） |
| 状态枚举 | `constants/IngredientStatus.java` | EXPIRED(0)/URGENT(1)/WARNING(2)/NORMAL(3) |
| 堆缓存 | `cache/IngredientHeapCache.java` | 三层最小堆，服务 expiringNotice |
| 定时任务 | `service/IngredientSchedulerService.java` | 30天全量刷新 + 每日迁移清理 + 微信推送 |

## CONVENTIONS

### 食材状态（四级制，动态计算，不入库）
```
IngredientStatus.compute(expireDate):
  expireDate == null        → NORMAL(3, "绿灯")
  daysLeft < 0              → EXPIRED(0, "已过期")
  0 ≤ daysLeft ≤ 3          → URGENT(1, "红灯")
  3 < daysLeft ≤ 15         → WARNING(2, "黄灯")
  daysLeft > 15             → NORMAL(3, "绿灯")
```

### 堆缓存（三层最小堆）
- 三个 PriorityQueue（按 expireDate 升序）：expiredHeap / urgentHeap / warningHeap
- NORMAL 不入堆
- 全量刷新：每 30 天从 MySQL 拉取 `expireDate ≤ 30天` 的食材 + 所有过期食材
- 增量同步：add/update/delete 实时同步堆
- 每日迁移：堆顶按当前状态重新分类，迁移的 `readed=false`
- 过期清理：`readed=true` 或过期超过 365 天的从 expiredHeap 移除
- 双实现：内存（`InMemoryIngredientHeapCache`）/ Redis ZSET（`RedisIngredientHeapCache`），通过 `flcr.cache.type` 切换

### 已读确认
- `Ingredient.readed`（TINYINT）：0=未确认，1=已确认
- 确认接口：单条 `POST /api/ingredient/{id}/read`，批量 `POST /api/ingredient/read`
- 自动重置：expireDate 变更时、堆迁移时 → `readed=false`

### 调味品复用模式
`CondimentController` 不是独立领域——无 `Condiment` entity/mapper，直接委托 `IngredientService`，固定 `category = "调味品"`。

### 批量添加
接收 `List<IngredientAddRequestDTO>`，循环插入，返回成功条数。

### 通知推送
- 每日迁移后触发微信订阅消息推送
- 模板字段：thing1(物品名称)、date2(到期日期)、thing8(温馨提醒)
- 配置：`flcr.notification.enabled` + `flcr.notification.wx-template-id`

## CONFIGURATION

```yaml
flcr:
  cache:
    type: memory        # memory | redis
  notification:
    enabled: false      # 是否启用微信推送
    wx-template-id: ""  # 订阅消息模板 ID
```

## ANTI-PATTERNS

- ~~`IngredientController.update()` 缺少 `@Valid`~~: 已修复
- ~~`CondimentController.update()` 缺少 `@Valid`~~: 已修复
- **调味品不是独立领域**: 仅为 category 过滤层，无独立 entity/mapper
- **status 不入库**: 动态计算，不要试图在 Entity 或 SQL 中存 status
- **readed 在更新 expireDate 时必须重置**: 过期日变了，确认状态自然失效
