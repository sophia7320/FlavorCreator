package flcr.backend.common.service.impl;

import flcr.backend.common.config.StorageProperties;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "flcr.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final StorageProperties storageProperties;

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            String relativePath = url.getPath();
            Path filePath = Paths.get(storageProperties.getLocalPath(), relativePath);
            if (filePath.startsWith(storageProperties.getLocalPath())) {
                Files.deleteIfExists(filePath);
                log.info("本地文件已删除: {}", filePath);
            }
        } catch (Exception e) {
            log.error("本地文件删除失败: {}", fileUrl, e);
        }
    }

    @Override
    public String store(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上传文件为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String fileName = UUID.randomUUID().toString() + ext;

        Path baseDir = Paths.get(storageProperties.getLocalPath()).toAbsolutePath().normalize();
        Path targetDir = baseDir.resolve(dir).resolve(dateDir);

        try {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(fileName);
            file.transferTo(targetFile.toFile());

            String relativePath = "/" + dir + "/" + dateDir + "/" + fileName;
            String url = storageProperties.getBaseUrl() != null && !storageProperties.getBaseUrl().isEmpty()
                    ? storageProperties.getBaseUrl() + storageProperties.getUrlPrefix() + relativePath
                    : storageProperties.getUrlPrefix() + relativePath;

            log.info("文件上传成功: {}", url);
            return url;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件上传失败");
        }
    }
}
