# 用户信息模块设计文档

**日期**: 2026-04-28  
**模块**: `flcr.backend.user`  
**依据**: 《创味机 API 接口文档》第三节"用户信息模块"

---

## 1. 数据模型

### User Entity 修改（auth 模块）
新增 `preferences` 字段（JSON 列，存储用户偏好设置）：

```java
private String preferences;  // JSON: { taste: [], dietary: [], cookTime, difficulty }
```

### preferences JSON 结构
```json
{
  "taste": ["清淡", "少油"],
  "dietary": ["低卡"],
  "cookTime": 30,
  "difficulty": "简单"
}
```

---

## 2. 模块结构

```
flcr.backend.user/
├── DTO/request/UpdateUserInfoRequestDTO.java
├── DTO/response/UserInfoResponseDTO.java
├── service/UserInfoService.java
├── service/impl/UserInfoServiceImpl.java    ← 注入 UserMapper（跨模块）
└── controller/UserInfoController.java       /api/user
```

---

## 3. API

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/user/info` | 必须 | 获取当前用户完整信息（含偏好 + 统计） |
| POST | `/api/user/info` | 必须 | 更新个人信息（昵称/签名/背景/性别/偏好） |
| POST | `/api/user/avatar` | 必须 | 上传头像（multipart），返回 `{avatarUrl}` |
| POST | `/api/user/background` | 必须 | 上传背景图，返回 `{backgroundUrl}` |

---

## 4. 关键设计

- **preferences 读写**：ObjectMapper 序列化/反序列化
- **stats 统计**：likeCount/collectionCount 查询 like/collection 表；followingCount/followerCount 暂返回 0
- **头像/背景上传**：占位符实现（`/uploads/avatar.jpg`），后续统一对接 OSS
- **手机号脱敏**：返回 `138****0000` 格式
- **跨模块数据**：注入 `UserMapper`（auth 模块），遵循 AGENTS.md 约定
