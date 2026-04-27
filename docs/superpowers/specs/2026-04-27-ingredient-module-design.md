# 食材管理模块设计文档

**日期**: 2026-04-27  
**模块**: `flcr.backend.ingredient`  
**依据**: 《创味机 API 接口文档》第五节"食材管理模块"

---

## 1. 数据库设计

### 1.1 食材表 `ingredient`

```sql
CREATE TABLE IF NOT EXISTS `ingredient` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`           BIGINT        NOT NULL                COMMENT '用户ID',
    `name`              VARCHAR(64)   NOT NULL                COMMENT '食材名称',
    `quantity`          DECIMAL(10,2) NOT NULL  DEFAULT 0     COMMENT '数量',
    `unit`              VARCHAR(16)   NOT NULL  DEFAULT ''    COMMENT '单位',
    `category`          VARCHAR(32)   NOT NULL  DEFAULT ''    COMMENT '分类',
    `storage_condition` VARCHAR(32)            DEFAULT ''    COMMENT '存储条件',
    `expire_date`       DATE                    DEFAULT NULL  COMMENT '保质期',
    `created_at`        DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_expire_date` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- 调味品与食材共用此表，通过 `category = '调味品'` 区分
- `status` 不在数据库中存储，查询时根据 `expire_date` 动态计算

### 1.2 常用食材表 `common_ingredient`

```sql
CREATE TABLE IF NOT EXISTS `common_ingredient` (
    `id`            INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category`      VARCHAR(32) NOT NULL              COMMENT '分类',
    `name`          VARCHAR(64) NOT NULL              COMMENT '食材名称',
    `default_unit`  VARCHAR(16) NOT NULL DEFAULT ''   COMMENT '默认单位',
    PRIMARY KEY (`id`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- 系统预设数据，不关联 user_id
- 预置了 65 条常见食材，覆盖蔬菜、肉类、水产、蛋类、乳制品、豆制品、谷物、调味品、水果、其他共 10 个分类

---

## 2. 模块结构

```
flcr.backend.ingredient/
├── entity/
│   ├── Ingredient.java              @TableName("`ingredient`")
│   └── CommonIngredient.java        @TableName("`common_ingredient`")
├── mapper/
│   ├── IngredientMapper.java         extends BaseMapper<Ingredient>
│   └── CommonIngredientMapper.java   extends BaseMapper<CommonIngredient>
├── service/
│   ├── IngredientService.java        接口，定义 7 个方法
│   └── impl/IngredientServiceImpl.java  @Service，实现类
├── DTO/
│   ├── request/
│   │   ├── IngredientAddRequestDTO.java
│   │   ├── IngredientUpdateRequestDTO.java
│   │   ├── IngredientBatchAddRequestDTO.java
│   │   └── IngredientListQueryDTO.java
│   └── response/
│       ├── IngredientResponseDTO.java
│       ├── IngredientListResponseDTO.java
│       ├── ExpiringNoticeResponseDTO.java
│       └── CommonIngredientResponseDTO.java
└── controller/
    ├── IngredientController.java          /api/ingredient
    └── CondimentController.java           /api/condiment（复用 IngredientService）
```

---

## 3. API 设计

### IngredientController (/api/ingredient)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/list` | 必须 | 食材列表，支持 sortBy/sort/status/category 筛选 |
| POST |  | 必须 | 添加食材 |
| PUT | `/{id}` | 必须 | 更新食材（部分字段） |
| DELETE | `/{id}` | 必须 | 删除食材（仅自己的） |
| POST | `/batch` | 必须 | 批量添加 |
| GET | `/expiring-notice` | 必须 | 临期/过期提醒 |
| GET | `/common` | 可选 | 常用食材分类列表 |

### CondimentController (/api/condiment)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST |  | 必须 | 添加调味品（category 固定为"调味品"） |
| GET | `/list` | 必须 | 调味品列表 |
| PUT | `/{id}` | 必须 | 更新调味品 |
| DELETE | `/{id}` | 必须 | 删除调味品 |

---

## 4. 关键业务逻辑

### 4.1 status 动态计算

```java
private String computeStatus(LocalDate expireDate) {
    if (expireDate == null) return "normal";
    long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expireDate);
    if (daysLeft < 0) return "expired";    // 已过期
    if (daysLeft <= 3) return "expiring";   // ≤3天临期
    return "normal";
}
```

### 4.2 调味品复用

`CondimentController` 直接注入 `IngredientService`，在 Controller 层将 DTO 的 `category` 固定为 `"调味品"`。所有业务逻辑与食材完全一致，零代码重复。

### 4.3 用户隔离

- Service 层通过 `UserContext.getUserId()` 获取当前用户
- 更新/删除时校验 `ingredient.userId == currentUserId`，不匹配则抛出 `BusinessException(PERMISSION_ERROR)`

### 4.4 临期提醒

`expiringNotice()` 返回两组数据：`expiring`（距保质期 ≤3 天）、`expired`（已过期），含汇总计数。

---

## 5. 约定遵行

| 约定 | 实际遵循 |
|------|---------|
| Mapper 不加 `@Mapper` | ✅ 仅继承 BaseMapper |
| Service 通过 UserContext 获取用户 | ✅ 不传 userId 参数 |
| 响应 DTO 四合一注解 | ✅ @Data @Builder @NoArgsConstructor @AllArgsConstructor |
| 写操作加 @Transactional | ✅ 在 add/update/delete/batchAdd 方法上 |
| 错误码使用 ResultCode 常量 | ✅ PARAM_ERROR / RESOURCE_NOT_EXIST / PERMISSION_ERROR |
| 建表用 snake_case + COMMENT | ✅ created_at/updated_at 含中文 COMMENT |

---

## 6. 测试

- **IngredientMapperTest**：9 个测试用例，覆盖增删改查、批量插入、条件筛选、排序、计数
- **CommonIngredientMapperTest**：4 个测试用例，覆盖全查、按分类查、综合分类查、字段完整性验证
- 测试模式：`@SpringBootTest` + `@ActiveProfiles("dev")` + `@Transactional`（自动回滚）
