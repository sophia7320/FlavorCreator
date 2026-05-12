package flcr.backend.common.service.impl;

import flcr.backend.common.config.ModerationProperties;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.ImageModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "flcr.moderation.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpModerationServiceImpl implements ImageModerationService {

    private final ModerationProperties moderationProperties;

    @Override
    public void validate(MultipartFile file, String scene) {
        validateFileType(file);
        validateFileSize(file, scene);
    }

    @Override
    public void moderate(String storageUrl, String scene) {
        log.debug("内容审核已跳过 (storage={}, scene={})", storageUrl, scene);
    }

    private void validateFileType(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new BusinessException(ResultCode.IMAGE_TYPE_ERROR, "图片格式不支持，仅支持 JPG/PNG/GIF/WEBP/BMP 格式");
        }
        String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        if (!moderationProperties.getAllowedTypes().contains(ext)) {
            throw new BusinessException(ResultCode.IMAGE_TYPE_ERROR, "图片格式不支持，仅支持 JPG/PNG/GIF/WEBP/BMP 格式");
        }
    }

    private void validateFileSize(MultipartFile file, String scene) {
        long maxSize = moderationProperties.getMaxSize().getOrDefault(scene, 5L * 1024 * 1024);
        if (file.getSize() > maxSize) {
            String sizeMB = (maxSize / (1024 * 1024)) + "MB";
            throw new BusinessException(ResultCode.IMAGE_SIZE_ERROR, "图片大小超出限制，最大支持 " + sizeMB);
        }
    }
}
