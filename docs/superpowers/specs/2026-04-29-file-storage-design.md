# 文件上传持久化存储 + 静态资源映射 — 设计文档

**日期**: 2026-04-29  
**目标**: 替换所有上传占位符，实现文件落盘和静态资源对外访问

---

## 1. 方案：策略模式 + 本地/OSS 双实现

```
FileStorageService (接口)
├── LocalFileStorageServiceImpl  (flcr.storage.type=local)
└── OssFileStorageServiceImpl    (flcr.storage.type=oss，占位)
```

通过 `@ConditionalOnProperty` 按配置文件切换实现。

---

## 2. 配置项

```yaml
flcr:
  storage:
    type: local                      # local | oss
    local-path: ./uploads            # 本地存储根目录
    url-prefix: /uploads             # 对外 URL 前缀
```

- `application-dev.yml`: type=local
- `application-prod.yml`: type=oss

---

## 3. 文件存储

### 接口
```java
public interface FileStorageService {
    String store(MultipartFile file, String dir);
}
```

### 本地实现
- 文件名：`UUID + 扩展名`（防路径遍历）
- 目录：`{localPath}/{dir}/{yyyyMM}/`
- 自动 mkdirs
- 返回：`{urlPrefix}/{dir}/{yyyyMM}/{uuid.ext}`

### OSS 占位实现
- 返回 `/uploads/placeholder.jpg`
- log.warn 提示尚未对接

---

## 4. 静态资源映射

```java
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    // file:{absolutePath} → /uploads/**
}
```

- 相对路径自动转绝对路径
- 仅在 local 模式下生效（OSS 模式文件在云端，不需要本地映射）

---

## 5. 受影响的上传点

| 文件 | 方法 | dir |
|------|------|-----|
| CommunityServiceImpl | publishRecipe | recipe-cover / recipe-image |
| UserInfoServiceImpl | uploadAvatar | avatar |
| UserInfoServiceImpl | uploadBackground | background |

---

## 6. 上传限制

```yaml
spring.servlet.multipart:
  max-file-size: 10MB
  max-request-size: 20MB
```

---

## 7. 改动文件清单

| 新建 | common/config/StorageProperties.java |
| 新建 | common/config/StaticResourceConfig.java |
| 新建 | common/service/FileStorageService.java |
| 新建 | common/service/impl/LocalFileStorageServiceImpl.java |
| 新建 | common/service/impl/OssFileStorageServiceImpl.java |
| 修改 | application.yml, application-dev.yml, application-prod.yml |
| 修改 | CommunityServiceImpl.java |
| 修改 | UserInfoServiceImpl.java |
