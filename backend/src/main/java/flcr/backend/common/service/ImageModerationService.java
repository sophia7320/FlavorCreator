package flcr.backend.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageModerationService {

    void validate(MultipartFile file, String scene);

    void moderate(String storageUrl, String scene);
}
