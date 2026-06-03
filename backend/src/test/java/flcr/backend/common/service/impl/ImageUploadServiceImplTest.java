package flcr.backend.common.service.impl;

import flcr.backend.common.constants.ImageScene;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.FileStorageService;
import flcr.backend.common.service.ImageModerationService;
import org.junit.jupiter.api.BeforeEach;
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
class ImageUploadServiceImplTest {

    @Mock
    private ImageModerationService imageModerationService;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private MultipartFile file;

    @InjectMocks
    private ImageUploadServiceImpl imageUploadService;

    @Test
    @DisplayName("upload - 正常流程：validate → store → moderate → 返回URL")
    void testUpload_Success() {
        String sceneValue = "avatar";
        String expectedUrl = "https://example.com/avatar/uuid.jpg";

        doNothing().when(imageModerationService).validate(file, sceneValue);
        when(fileStorageService.store(file, sceneValue)).thenReturn(expectedUrl);
        doNothing().when(imageModerationService).moderate(expectedUrl, sceneValue);

        String url = imageUploadService.upload(file, ImageScene.AVATAR);

        assertEquals(expectedUrl, url);
        verify(imageModerationService).validate(file, sceneValue);
        verify(fileStorageService).store(file, sceneValue);
        verify(imageModerationService).moderate(expectedUrl, sceneValue);
    }

    @Test
    @DisplayName("upload - validate 失败时抛异常，不执行 store")
    void testUpload_ValidateFails() {
        doThrow(new BusinessException("校验失败")).when(imageModerationService).validate(any(), any());

        assertThrows(BusinessException.class, () ->
                imageUploadService.upload(file, ImageScene.AVATAR));

        verify(fileStorageService, never()).store(any(), any());
        verify(imageModerationService, never()).moderate(any(), any());
    }

    @Test
    @DisplayName("upload - store 失败时抛异常，不执行 moderate")
    void testUpload_StoreFails() {
        doNothing().when(imageModerationService).validate(any(), any());
        when(fileStorageService.store(any(), any())).thenThrow(new RuntimeException("存储失败"));

        assertThrows(RuntimeException.class, () ->
                imageUploadService.upload(file, ImageScene.AVATAR));

        verify(imageModerationService).validate(file, "avatar");
        verify(fileStorageService).store(file, "avatar");
        verify(imageModerationService, never()).moderate(any(), any());
    }

    @Test
    @DisplayName("upload - moderate 失败时删除已存储文件并抛异常")
    void testUpload_ModerateFails_CleansUp() {
        String storedUrl = "https://example.com/bad.jpg";
        doNothing().when(imageModerationService).validate(any(), any());
        when(fileStorageService.store(any(), any())).thenReturn(storedUrl);
        doThrow(new BusinessException("审核不通过")).when(imageModerationService).moderate(eq(storedUrl), any());

        assertThrows(BusinessException.class, () ->
                imageUploadService.upload(file, ImageScene.RECIPE_COVER));

        verify(fileStorageService).delete(storedUrl);
    }

    @Test
    @DisplayName("upload - moderate 失败时删除文件也失败，仍抛原始审核异常")
    void testUpload_ModerateFails_DeleteAlsoFails() {
        String storedUrl = "https://example.com/bad.jpg";
        doNothing().when(imageModerationService).validate(any(), any());
        when(fileStorageService.store(any(), any())).thenReturn(storedUrl);
        doThrow(new BusinessException("审核不通过")).when(imageModerationService).moderate(eq(storedUrl), any());
        doThrow(new RuntimeException("删除失败")).when(fileStorageService).delete(storedUrl);

        assertThrows(BusinessException.class, () ->
                imageUploadService.upload(file, ImageScene.RECIPE_COVER));

        verify(fileStorageService).delete(storedUrl);
    }

    @Test
    @DisplayName("upload - 各 scene 均正确传递 sceneValue")
    void testUpload_DifferentScenes() {
        when(fileStorageService.store(any(), eq("background"))).thenReturn("url");
        imageUploadService.upload(file, ImageScene.BACKGROUND);
        verify(imageModerationService).validate(file, "background");
        verify(imageModerationService).moderate("url", "background");

        when(fileStorageService.store(any(), eq("recipe-image"))).thenReturn("url2");
        imageUploadService.upload(file, ImageScene.RECIPE_IMAGE);
        verify(imageModerationService).validate(file, "recipe-image");
        verify(imageModerationService).moderate("url2", "recipe-image");
    }
}
