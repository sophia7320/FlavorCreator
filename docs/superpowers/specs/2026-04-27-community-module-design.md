# 社区模块设计文档

**日期**: 2026-04-27  
**模块**: `flcr.backend.community`  
**依据**: 《创味机 API 接口文档》第六节"社区模块"

---

## 1. 数据库表

### `recipe` 表（跨模块引用自 recipe 模块）
- 含 `images`、`ingredients`、`steps`、`tags` 四个 JSON 列
- 设置 `autoResultMap = true`

### `comment` 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO | 主键 |
| user_id | BIGINT | 用户 ID |
| recipe_id | BIGINT | 菜谱 ID |
| parent_id | BIGINT | 父评论 ID |
| content | TEXT | 评论内容 |
| like_count | INT | 点赞数 |
| created_at / updated_at | DATETIME | 时间 |

### `like` 表
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 组合唯一键 |
| target_type | TINYINT | 1-菜谱 2-评论 |
| target_id | BIGINT | 目标 ID |

### `collection` 表
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 组合唯一键 |
| recipe_id | BIGINT | 菜谱 ID |

---

## 2. 模块结构

```
flcr.backend.community/
├── entity/Comment.java / Like.java / Collection.java
├── mapper/CommentMapper / LikeMapper / CollectionMapper   (无 @Mapper)
├── DTO/request/、DTO/response/
├── service/CommunityService.java + impl/CommunityServiceImpl.java
└── controller/CommunityController.java  /api/community，@Slf4j
```

---

## 3. API

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/community/recipe` | 必须 | 发布菜谱（multipart，DTO用 @RequestPart） |
| GET | `/api/community/recipe/list` | 无 | 菜谱列表 |
| GET | `/api/community/recipe/{id}` | 可选 | 菜谱详情 |
| POST | `/api/community/recipe/{id}/like` | 必须 | 点赞 |
| DELETE | `/api/community/recipe/{id}/like` | 必须 | 取消点赞 |
| POST | `/api/community/recipe/{id}/collect` | 必须 | 收藏 |
| DELETE | `/api/community/recipe/{id}/collect` | 必须 | 取消收藏 |
| GET | `/api/community/recipe/{id}/comment` | 可选 | 评论列表 |
| POST | `/api/community/recipe/{id}/comment` | 必须 | 发表评论 |
| DELETE | `/api/community/comment/{id}` | 必须 | 删除评论 |
| POST | `/api/community/comment/{id}/like` | 必须 | 评论点赞 |
| DELETE | `/api/community/comment/{id}/like` | 必须 | 取消评论点赞 |

---

## 4. 关键设计决策

- **用户获取**：Service 层通过 `UserContext.getUserId()` 获取（已修正，不再从 Controller 传参）
- **跨模块引用**：注入 `UserMapper`（auth 模块）和 `RecipeMapper`（recipe 模块）
- **JSON 读写**：使用 `objectMapper.writeValueAsString()` / `readValue()`
- **并发安全**：点赞/收藏/评论/浏览计数使用数据库原子操作 `SET count = count ± 1`，消除 lost-update 竞态
- **待办**：文件上传为占位符、评论点赞逻辑
