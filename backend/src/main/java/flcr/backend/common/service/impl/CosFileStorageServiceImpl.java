package flcr.backend.common.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import flcr.backend.common.config.StorageProperties;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "flcr.storage.type", havingValue = "cos")
public class CosFileStorageServiceImpl implements FileStorageService {

    private final StorageProperties storageProperties;

    @Override
    public String store(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上传文件为空");
        }

        StorageProperties.Cos cos = storageProperties.getCos();
        COSCredentials cred = new BasicCOSCredentials(cos.getSecretId(), cos.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cos.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String fileName = UUID.randomUUID().toString() + ext;
            String key = dir + "/" + dateDir + "/" + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putRequest = new PutObjectRequest(
                    cos.getBucket(), key,
                    file.getInputStream(), metadata
            );

            putRequest.setCannedAcl(CannedAccessControlList.PublicRead);
            cosClient.putObject(putRequest);

            String url = String.format("https://%s.cos.%s.myqcloud.com/%s",
                    cos.getBucket(), cos.getRegion(), key);

            log.info("COS 文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("COS 文件上传失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            cosClient.shutdown();
        }
    }
}
