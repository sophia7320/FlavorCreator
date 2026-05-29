package flcr.backend.common.service.impl;

import flcr.backend.common.config.ModerationProperties;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoOpModerationServiceImplTest {

    @Mock private ModerationProperties moderationProperties;
    @InjectMocks private NoOpModerationServiceImpl moderationService;

    @BeforeEach
    void setUp() {
        lenient().when(moderationProperties.getAllowedTypes())
                .thenReturn(List.of("jpg", "jpeg", "png", "gif", "webp", "bmp"));
        lenient().when(moderationProperties.getMaxSize())
                .thenReturn(Map.of(
                        "avatar", 2L * 1024 * 1024,
                        "background", 5L * 1024 * 1024,
                        "recipe-cover", 5L * 1024 * 1024,
                        "recipe-image", 5L * 1024 * 1024
                ));
    }

    @Test
    @DisplayName("validate - JPG格式通过")
    void testValidate_ValidJpg() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(100_000L);

        assertDoesNotThrow(() -> moderationService.validate(file, "avatar"));
    }

    @Test
    @DisplayName("validate - PNG格式通过")
    void testValidate_ValidPng() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("icon.png");
        when(file.getSize()).thenReturn(50_000L);

        assertDoesNotThrow(() -> moderationService.validate(file, "background"));
    }

    @Test
    @DisplayName("validate - WEBP格式通过")
    void testValidate_ValidWebp() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.webp");
        when(file.getSize()).thenReturn(200_000L);

        assertDoesNotThrow(() -> moderationService.validate(file, "recipe-image"));
    }

    @Test
    @DisplayName("validate - BMP格式通过")
    void testValidate_ValidBmp() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.bmp");
        when(file.getSize()).thenReturn(100_000L);

        assertDoesNotThrow(() -> moderationService.validate(file, "avatar"));
    }

    @Test
    @DisplayName("validate - 不支持格式抛2001异常")
    void testValidate_InvalidType() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.validate(file, "avatar"));
        assertEquals(ResultCode.IMAGE_TYPE_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("validate - 无扩展名抛2001异常")
    void testValidate_NoExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("nofile");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.validate(file, "avatar"));
        assertEquals(ResultCode.IMAGE_TYPE_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("validate - 空原始名抛2001异常")
    void testValidate_NullFilename() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.validate(file, "avatar"));
        assertEquals(ResultCode.IMAGE_TYPE_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("validate - 头像2MB内通过")
    void testValidate_AvatarWithinLimit() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(2L * 1024 * 1024 - 1);

        assertDoesNotThrow(() -> moderationService.validate(file, "avatar"));
    }

    @Test
    @DisplayName("validate - 头像超2MB抛2002异常")
    void testValidate_AvatarExceedsLimit() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(2L * 1024 * 1024 + 1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.validate(file, "avatar"));
        assertEquals(ResultCode.IMAGE_SIZE_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("validate - 菜谱封面5MB内通过")
    void testValidate_RecipeCoverWithinLimit() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("recipe.jpg");
        when(file.getSize()).thenReturn(4L * 1024 * 1024);

        assertDoesNotThrow(() -> moderationService.validate(file, "recipe-cover"));
    }

    @Test
    @DisplayName("validate - 菜谱封面超5MB抛2002异常")
    void testValidate_RecipeCoverExceedsLimit() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("recipe.jpg");
        when(file.getSize()).thenReturn(6L * 1024 * 1024);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.validate(file, "recipe-cover"));
        assertEquals(ResultCode.IMAGE_SIZE_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("moderate - 直接通过（无审核）")
    void testModerate_NoOp() {
        assertDoesNotThrow(() -> moderationService.moderate("/uploads/avatar/test.jpg", "avatar"));
    }

    @Test
    @DisplayName("moderate - 不同场景均无异常")
    void testModerate_VariousScenes() {
        assertDoesNotThrow(() -> moderationService.moderate("/uploads/background/test.jpg", "background"));
        assertDoesNotThrow(() -> moderationService.moderate("/uploads/recipe-cover/test.jpg", "recipe-cover"));
        assertDoesNotThrow(() -> moderationService.moderate("/uploads/recipe-image/test.jpg", "recipe-image"));
    }
}
