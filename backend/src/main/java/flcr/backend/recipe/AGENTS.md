# recipe/ — 菜谱模块

**模块**: `flcr.backend.recipe` | 菜谱发布/列表/详情，图片通过 `/api/image/upload` 独立上传。

## STRUCTURE

```
recipe/
├── controller/     # RecipeController（8 个端点: publish/list/detail/apply/generate/recommend/update/delete）
├── DTO/request/    # CreateRecipeRequestDTO, RecipeListRequestDTO, RecipeUpdateRequestDTO 等
├── DTO/response/   # RecipeDetailDTO, RecipeListItemDTO 等
├── entity/         # Recipe（images 用 JacksonTypeHandler，ingredients/steps/tags 仍为 JSON String）
├── mapper/         # RecipeMapper（extends BaseMapper）
├── client/         # LlmClient（LLM API 调用，RestTemplate 超时 5s/30s）
├── util/           # DifficultyUtil（难度转换）、RecipeValidator（校验）、RecipeDtoAssembler（DTO 组装）
└── service/
    ├── RecipeService（外观接口，7 方法）
    ├── RecipeWriteService（发布/更新/删除）
    ├── RecipeQueryService（列表/详情）
    ├── RecipeMatchService（食材匹配推荐）
    ├── RecipeRecommendService（LLM 推荐）
    ├── RecipeGenerateService（AI 生成）
    └── impl/RecipeServiceImpl（外观实现，委托给 4 子 Service）
```

## WHERE TO LOOK

| 需求 | 位置 | 说明 |
|------|------|------|
| 发布菜谱 | `RecipeServiceImpl.publishRecipe()` | 纯 JSON 请求，imageUrls/coverUrl 需预先通过 `/api/image/upload` 上传 |
| 菜谱列表（分页+筛选） | `RecipeServiceImpl.getRecipeList()` | @Public，category/difficulty/keyword 筛选 |
| 菜谱详情 | `RecipeServiceImpl.getRecipeDetail()` | @Public，登录则回传点赞/收藏状态 |
| 菜谱实体 JSON 字段 | `entity/Recipe.java` | images/steps/ingredients/tags 均为 JSON String |
| 列表筛选 DTO | `DTO/request/RecipeListRequestDTO.java` | taste 字段已定义但未实现 |

## CONVENTIONS

### JSON 字段处理
- Recipe 的 `images`、`steps`、`ingredients`、`tags` 在 Entity 中均为 `String`
- 发布时: `objectMapper.writeValueAsString(obj)`
- 读取时: `objectMapper.readValue(jsonStr, new TypeReference<>(){})`

## ANTI-PATTERNS

- **`taste` 字段已定义但未使用**: `RecipeListRequestDTO.taste` 在 `getRecipeList()` 中未参与筛选 — 需实现或删除
