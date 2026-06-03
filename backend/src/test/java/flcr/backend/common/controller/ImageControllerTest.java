package flcr.backend.common.controller;

import flcr.backend.common.constants.ImageScene;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.ImageUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageUploadService imageUploadService;
    @Mock
    private MultipartFile file;

    @InjectMocks
    private ImageController controller;

    @Test
    @DisplayName("upload - avatar场景返回URL")
    void testUpload_Avatar() {
        when(imageUploadService.upload(file, ImageScene.AVATAR)).thenReturn("/uploads/avatar/test.jpg");

        var response = controller.upload(file, "avatar");
        assertEquals("/uploads/avatar/test.jpg", response.getData().get("url"));
    }

    @Test
    @DisplayName("upload - background场景返回URL")
    void testUpload_Background() {
        when(imageUploadService.upload(file, ImageScene.BACKGROUND)).thenReturn("/uploads/bg/test.jpg");

        var response = controller.upload(file, "background");
        assertEquals("/uploads/bg/test.jpg", response.getData().get("url"));
    }

    @Test
    @DisplayName("upload - 无效scene抛异常")
    void testUpload_InvalidScene() {
        assertThrows(BusinessException.class, () ->
                controller.upload(file, "invalid"));
        verify(imageUploadService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("upload - recipe-cover场景返回URL")
    void testUpload_RecipeCover() {
        when(imageUploadService.upload(file, ImageScene.RECIPE_COVER)).thenReturn("/uploads/recipe-cover/test.jpg");

        var response = controller.upload(file, "recipe-cover");
        assertEquals("/uploads/recipe-cover/test.jpg", response.getData().get("url"));
    }
}
