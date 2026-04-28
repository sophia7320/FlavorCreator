package flcr.backend.common.service.impl;

import flcr.backend.common.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@ConditionalOnProperty(name = "flcr.storage.type", havingValue = "oss")
public class OssFileStorageServiceImpl implements FileStorageService {

    @Override
    public String store(MultipartFile file, String dir) {
        log.warn("OSS 存储尚未对接，返回占位 URL");
        return "/uploads/placeholder.jpg";
    }
}
