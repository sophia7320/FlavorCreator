package flcr.backend.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file, String dir);

    /**
     * 删除已存储的文件（用于事务回滚时的补偿清理）
     */
    default void delete(String fileUrl) {
        // 默认不实现，由具体存储实现类覆盖
    }
}
