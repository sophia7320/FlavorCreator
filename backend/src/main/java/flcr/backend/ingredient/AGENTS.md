# ingredient/ — 食材与调味品模块

**模块**: `flcr.backend.ingredient` | 食材 CRUD、临期提醒、常用食材库、调味品（复用食材逻辑）。

## STRUCTURE

```
ingredient/
├── controller/     # IngredientController + CondimentController（复用 IngredientService）
├── DTO/request/    # IngredientAddRequestDTO, IngredientUpdateRequestDTO, IngredientBatchAddRequestDTO, IngredientListQueryDTO
├── DTO/response/   # IngredientResponseDTO, IngredientListResponseDTO, CommonIngredientResponseDTO, ExpiringNoticeResponseDTO
├── entity/         # Ingredient, CommonIngredient
├── mapper/         # IngredientMapper, CommonIngredientMapper
└── service/
    ├── IngredientService（接口）
    └── impl/IngredientServiceImpl
```

## WHERE TO LOOK

| 需求 | 文件 | 说明 |
|------|------|------|
| 添加/更新/删除食材 | `controller/IngredientController.java` → `IngredientServiceImpl` | PUT 已加 `@Valid` |
| 批量添加食材 | `controller/IngredientController.java` | `POST /api/ingredient/batch` |
| 临期/过期提醒 | `controller/IngredientController.java` | `GET /api/ingredient/expiring-notice` |
| 常用食材分类 | `controller/IngredientController.java` | `GET /api/ingredient/common`，游客可访问 |
| 调味品接口 | `controller/CondimentController.java` | 复用 IngredientService，category 固定为"调味品" |
| 食材实体 | `entity/Ingredient.java` | 含 expire_date，status 动态计算（不入库） |

## CONVENTIONS

### 状态计算（动态，不入库）
```java
// status: normal / expiring / expired
// 依据 expire_date 与当前日期对比计算
```

### 调味品复用模式
`CondimentController` 不是独立领域——无 `Condiment` entity/mapper，直接委托 `IngredientService`，固定 `category = "调味品"`。

### 批量添加
接收 `List<IngredientAddRequestDTO>`，循环插入，返回成功条数。

## ANTI-PATTERNS

- ~~`IngredientController.update()` 缺少 `@Valid`~~: 已修复
- ~~`CondimentController.update()` 缺少 `@Valid`~~: 已修复
- **调味品不是独立领域**: 仅为 category 过滤层，无独立 entity/mapper
