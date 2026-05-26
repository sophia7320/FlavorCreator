# community/ — 社区模块

**模块**: `flcr.backend.community` | 菜谱点赞/收藏/评论，使用 toggle 模式（插入+递增/删除+递减）。

## STRUCTURE

```
community/
├── controller/     # CommunityController（1 个，10 个端点）
├── DTO/request/    # CommentRequestDTO
├── DTO/response/   # CommentResponseDTO, LikeCollectResponseDTO
├── entity/         # Comment, Like, Collection（3 个）
├── mapper/         # CommentMapper, LikeMapper, CollectionMapper（3 个）
└── service/
    ├── CommunityService（接口）
    └── impl/CommunityServiceImpl（14 方法, 402 行 — 最大文件）
```

## WHERE TO LOOK

| 需求 | 位置 | 说明 |
|------|------|------|
| 点赞/取消点赞菜谱 | `CommunityServiceImpl.toggleLike()` | insert(like_count+1) / delete(like_count-1) |
| 收藏/取消收藏 | `CommunityServiceImpl.toggleCollect()` | 同上 toggle 模式 |
| 评论列表（含嵌套回复） | `CommunityServiceImpl.getComments()` | 7 步流水线（见下方） |
| 发表评论 | `CommunityServiceImpl.createComment()` | 一级评论或回复（parentId） |
| 点赞/取消点赞评论 | `CommunityServiceImpl.toggleCommentLike()` | 独立于菜谱点赞 |
| 我的点赞/收藏状态 | `CommunityServiceImpl.checkLiked()` / `checkCollected()` | 返回 boolean |

## CONVENTIONS

### Toggle 模式（点赞/收藏）
```
插入 Like 记录 → .setSql("like_count = like_count + 1") 原子递增
删除 Like 记录 → .setSql("like_count = like_count - 1") 原子递减
```
通过 MySQL `unique(user_id, target_type, target_id)` 约束防重复。

### 评论嵌套查询（getComments 7 步流水线）
1. 查询一级评论（parentId=null）
2. 收集所有评论 ID → `parentIds`
3. 批量查询子回复：`.in(Comment::getParentId, parentIds)`
4. 按 parentId 分组子回复 → `Map<Long, List<CommentDTO>>`
5. 收集所有 userId（一级+回复）→ 批量查 User
6. 批量检查当前用户点赞状态
7. 组装 DTO

## ANTI-PATTERNS / KNOWN ISSUES

- ~~**`getComments()` 并发 Bug**: 使用实例字段 `userMap`~~: 已修复，`userMap` 已改为方法局部变量
- **`buildSingleCommentDTO()` N+1 查询**: 流中每层调 `userMapper.selectById`（不同于批量的 `buildCommentDTO`）
- ~~**`targetType` 魔法数字**~~: 已提取为 `TargetTypeConstants`（`RECIPE=1` / `COMMENT=2`）
- **`selectCount() > 0` 模式**: 存在性检查应改用 `selectOne()` 带 limit=1
