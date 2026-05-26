# recipe/ — 菜谱模块

**模块**: `flcr.backend.recipe` | 菜谱发布/列表/详情，含 5 阶段图片上传审核流水线。

## STRUCTURE

```
recipe/
├── controller/     # RecipeController（3 个端点: publish/list/detail）
├── DTO/request/    # PublishRecipeRequestDTO, RecipeListRequestDTO
├── DTO/response/   # RecipeDetailDTO, RecipeListItemDTO
├── entity/         # Recipe（含多个 JSON 字段: images, ingredients, steps, tags）
├── mapper/         # RecipeMapper
└── service/
    ├── RecipeService（接口）
    └── impl/RecipeServiceImpl（10 方法, 320 行）
```

## WHERE TO LOOK

| 需求 | 位置 | 说明 |
|------|------|------|
| 发布菜谱 | `RecipeServiceImpl.publishRecipe()` | multipart 上传，5 阶段流水线（见下方） |
| 菜谱列表（分页+筛选） | `RecipeServiceImpl.getRecipeList()` | @Public，category/difficulty/keyword 筛选 |
| 菜谱详情 | `RecipeServiceImpl.getRecipeDetail()` | @Public，登录则回传点赞/收藏状态 |
| 菜谱实体 JSON 字段 | `entity/Recipe.java` | images/steps/ingredients/tags 均为 JSON String |
| 列表筛选 DTO | `DTO/request/RecipeListRequestDTO.java` | taste 字段已定义但未实现 |
| Multipart 请求解析 | `controller/RecipeController.java` | `@RequestPart("request")` + `@RequestParam` |

## CONVENTIONS

### 图片上传审核流水线（5 阶段）
```
1. validate(coverFile, "recipe-cover")           — 校验封面类型+大小
2. store(coverFile, "recipe/covers")             — 上传封面到存储
3. for each image: validate(img, "recipe-image")  — 逐个校验图片
4. for each image: store(img, "recipe/images")    — 逐个上传图片
5. moderate(coverUrl) + for each: moderate(imgUrl) — 审核所有图片内容
```
- 任一阶段失败 → 已上传文件通过 `FileStorageService.delete()` 补偿清理
- 补偿在 `finally` 块中执行
- 整个方法 `@Transactional`：DB 入库和文件操作在同一事务

### JSON 字段处理
- Recipe 的 `images`、`steps`、`ingredients`、`tags` 在 Entity 中均为 `String`
- 发布时: `objectMapper.writeValueAsString(obj)`
- 读取时: `objectMapper.readValue(jsonStr, new TypeReference<>(){})`

## ANTI-PATTERNS

- **`taste` 字段已定义但未使用**: `RecipeListRequestDTO.taste` 在 `getRecipeList()` 中未参与筛选 — 需实现或删除
- **批量图片孤儿文件**: 若第 N 张审核失败，前 N-1 张 COS 文件成为孤儿（DB 回滚但文件无法回滚）— 当前可接受，长期应延后 upload
