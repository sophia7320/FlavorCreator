package flcr.backend.common.service.impl;

import flcr.backend.common.config.StorageProperties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalFileStorageServiceImplTest {

    @Mock private StorageProperties storageProperties;
    @InjectMocks private LocalFileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        lenient().when(storageProperties.getLocalPath()).thenReturn("./target/test-uploads");
        lenient().when(storageProperties.getUrlPrefix()).thenReturn("/uploads");
    }

    @AfterEach
    void tearDown() throws Exception {
        Path testDir = Paths.get("./target/test-uploads");
        if (Files.exists(testDir)) {
            Files.walk(testDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> p.toFile().delete());
        }
    }

    @Test
    @DisplayName("上传文件返回URL")
    void testStore_Success() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.jpg");

        String url = fileStorageService.store(file, "avatar");
        assertTrue(url.startsWith("/uploads/avatar/"));
        assertTrue(url.endsWith(".jpg"));
    }

    @Test
    @DisplayName("空文件抛异常")
    void testStore_EmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(flcr.backend.common.exception.BusinessException.class,
                () -> fileStorageService.store(file, "avatar"));
    }

    @Test
    @DisplayName("无扩展名文件正常保存")
    void testStore_NoExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("nofile");

        String url = fileStorageService.store(file, "recipe-cover");
        assertTrue(url.startsWith("/uploads/recipe-cover/"));
    }
}
