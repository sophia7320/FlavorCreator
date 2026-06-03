package flcr.backend.common.service.impl;

import flcr.backend.common.constants.ImageScene;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.FileStorageService;
import flcr.backend.common.service.ImageModerationService;
import flcr.backend.common.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 统一图片上传实现。
 * validate → store → moderate，store 失败不清理（无文件），
 * moderate 失败则删除已存储文件后抛异常。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadServiceImpl implements ImageUploadService {

    private final ImageModerationService imageModerationService;
    private final FileStorageService fileStorageService;

    @Override
    public String upload(MultipartFile file, ImageScene scene) {
        String sceneValue = scene.getValue();

        // Step 1: Validate (type + size)
        imageModerationService.validate(file, sceneValue);

        // Step 2: Store
        String url;
        try {
            url = fileStorageService.store(file, sceneValue);
        } catch (Exception e) {
            log.error("文件存储失败: scene={}", sceneValue, e);
            throw e instanceof BusinessException
                    ? (BusinessException) e
                    : new BusinessException(e.getMessage());
        }

        // Step 3: Moderate (content check)
        try {
            imageModerationService.moderate(url, sceneValue);
        } catch (Exception e) {
            // Clean up stored file on moderation failure
            log.warn("内容审核失败，删除已存储文件: url={}, scene={}", url, sceneValue);
            try {
                fileStorageService.delete(url);
            } catch (Exception delEx) {
                log.error("清理失败文件异常: url={}", url, delEx);
            }
            throw e instanceof BusinessException
                    ? (BusinessException) e
                    : new BusinessException(e.getMessage());
        }

        return url;
    }
}
