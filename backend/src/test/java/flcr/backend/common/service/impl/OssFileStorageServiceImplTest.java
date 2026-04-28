package flcr.backend.common.service.impl;

import flcr.backend.common.service.FileStorageService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OssFileStorageServiceImplTest {

    private final OssFileStorageServiceImpl service = new OssFileStorageServiceImpl();

    @Test
    @DisplayName("OSS占位实现返回placeholder")
    void testStore_ReturnsPlaceholder() {
        MultipartFile file = mock(MultipartFile.class);
        assertEquals("/uploads/placeholder.jpg", service.store(file, "avatar"));
    }
}
