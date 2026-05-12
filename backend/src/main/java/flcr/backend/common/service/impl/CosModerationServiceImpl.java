package flcr.backend.common.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ciModel.auditing.ImageAuditingRequest;
import com.qcloud.cos.model.ciModel.auditing.ImageAuditingResponse;
import com.qcloud.cos.region.Region;
import flcr.backend.common.config.ModerationProperties;
import flcr.backend.common.config.StorageProperties;
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
@ConditionalOnProperty(name = "flcr.moderation.enabled", havingValue = "true")
public class CosModerationServiceImpl implements ImageModerationService {

    private final ModerationProperties moderationProperties;
    private final StorageProperties storageProperties;

    @Override
    public void validate(MultipartFile file, String scene) {
        validateFileType(file);
        validateFileSize(file, scene);
    }

    @Override
    public void moderate(String storageUrl, String scene) {
        StorageProperties.Cos cos = storageProperties.getCos();
        String key = extractKeyFromUrl(storageUrl, cos);
        if (key == null) {
            log.warn("无法从存储 URL 提取对象键，跳过内容审核: {}", storageUrl);
            return;
        }

        COSClient cosClient = buildCosClient(cos);
        try {
            ImageAuditingRequest request = new ImageAuditingRequest();
            request.setBucketName(cos.getBucket());
            request.setObjectKey(key);
            request.setDetectType("Porn,Terrorism,Politics,Ads,Illegal,Abuse");

            ImageAuditingResponse response = cosClient.imageAuditing(request);

            String result = response.getResult();
            String label = response.getLabel();
            String score = response.getScore();

            if ("2".equals(result)) {
                log.warn("图片审核未通过 - label={}, score={}, key={}", label, score, key);
                try {
                    cosClient.deleteObject(cos.getBucket(), key);
                    log.info("已删除违规图片: {}", key);
                } catch (Exception delEx) {
                    log.error("删除违规图片失败，需手动清理: {}", key, delEx);
                }
                throw new BusinessException(ResultCode.IMAGE_MODERATION_FAILED, "图片包含违规内容，请重新选择");
            }

            if ("1".equals(result)) {
                log.info("图片需人工复审 - label={}, score={}, key={}", label, score, key);
            }

            log.debug("图片审核通过 - result={}, label={}, key={}", result, label, key);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("图片内容审核调用失败", e);
            throw new BusinessException(ResultCode.IMAGE_UPLOAD_ERROR, "图片审核服务异常，请稍后重试");
        } finally {
            cosClient.shutdown();
        }
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

    private String extractKeyFromUrl(String url, StorageProperties.Cos cos) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            String bucketEndpoint = String.format("https://%s.cos.%s.myqcloud.com/",
                    cos.getBucket(), cos.getRegion());
            if (url.startsWith(bucketEndpoint)) {
                return url.substring(bucketEndpoint.length());
            }
            log.warn("URL 非 COS 存储地址，跳过内容审核: {}", url);
            return null;
        } catch (Exception e) {
            log.warn("解析 COS URL 失败: {}", url, e);
            return null;
        }
    }

    COSClient buildCosClient(StorageProperties.Cos cos) {
        COSCredentials cred = new BasicCOSCredentials(cos.getSecretId(), cos.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cos.getRegion()));
        return new COSClient(cred, clientConfig);
    }
}
