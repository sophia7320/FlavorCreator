package flcr.backend.common.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ciModel.auditing.ImageAuditingRequest;
import com.qcloud.cos.model.ciModel.auditing.ImageAuditingResponse;
import flcr.backend.common.config.ModerationProperties;
import flcr.backend.common.config.StorageProperties;
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
class CosModerationServiceImplTest {

    @Mock private ModerationProperties moderationProperties;
    @Mock private StorageProperties storageProperties;
    @Mock private COSClient cosClient;
    @InjectMocks @Spy private CosModerationServiceImpl moderationService;

    private static final String BUCKET = "test-bucket-1250000000";
    private static final String REGION = "ap-shanghai";
    private static final String COS_URL = "https://test-bucket-1250000000.cos.ap-shanghai.myqcloud.com/avatar/202605/uuid.jpg";
    private static final String OBJECT_KEY = "avatar/202605/uuid.jpg";

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

        StorageProperties.Cos cos = mock(StorageProperties.Cos.class);
        lenient().when(storageProperties.getCos()).thenReturn(cos);
        lenient().when(cos.getBucket()).thenReturn(BUCKET);
        lenient().when(cos.getRegion()).thenReturn(REGION);
        lenient().when(cos.getSecretId()).thenReturn("test-id");
        lenient().when(cos.getSecretKey()).thenReturn("test-key");

        lenient().doReturn(cosClient).when(moderationService).buildCosClient(any());
    }

    // ==================== validate() tests ====================

    @Test
    @DisplayName("validate - JPG格式通过")
    void testValidate_ValidJpg() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(100_000L);

        assertDoesNotThrow(() -> moderationService.validate(file, "avatar"));
    }

    @Test
    @DisplayName("validate - 不支持格式抛2001异常")
    void testValidate_InvalidType() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("script.exe");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.validate(file, "avatar"));
        assertEquals(ResultCode.IMAGE_TYPE_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("validate - 头像超限抛2002异常")
    void testValidate_SizeExceeded() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(5L * 1024 * 1024);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.validate(file, "avatar"));
        assertEquals(ResultCode.IMAGE_SIZE_ERROR, ex.getCode());
    }

    // ==================== moderate() — URL skipping tests ====================

    @Test
    @DisplayName("moderate - URL为空直接返回")
    void testModerate_NullUrl() {
        assertDoesNotThrow(() -> moderationService.moderate(null, "avatar"));
    }

    @Test
    @DisplayName("moderate - 非COS URL跳过审核")
    void testModerate_UnparseableUrl() {
        assertDoesNotThrow(() ->
                moderationService.moderate("https://other-domain.com/some/file.jpg", "avatar"));
    }

    // ==================== moderate() — COS CI result tests ====================

    @Test
    @DisplayName("moderate - 审核通过 (result=0) 无异常")
    void testModerate_ResultPass() {
        ImageAuditingResponse response = mock(ImageAuditingResponse.class);
        when(response.getResult()).thenReturn("0");
        when(response.getLabel()).thenReturn("Normal");
        when(response.getScore()).thenReturn("0");
        when(cosClient.imageAuditing(any(ImageAuditingRequest.class))).thenReturn(response);

        assertDoesNotThrow(() -> moderationService.moderate(COS_URL, "avatar"));
    }

    @Test
    @DisplayName("moderate - 需人工复审 (result=1) 无异常仅记录日志")
    void testModerate_ResultReview() {
        ImageAuditingResponse response = mock(ImageAuditingResponse.class);
        when(response.getResult()).thenReturn("1");
        when(response.getLabel()).thenReturn("Porn");
        when(response.getScore()).thenReturn("80");
        when(cosClient.imageAuditing(any(ImageAuditingRequest.class))).thenReturn(response);

        assertDoesNotThrow(() -> moderationService.moderate(COS_URL, "avatar"));
    }

    @Test
    @DisplayName("moderate - 审核不通过 (result=2) 抛2003异常并删除文件")
    void testModerate_ResultBlock() {
        ImageAuditingResponse response = mock(ImageAuditingResponse.class);
        when(response.getResult()).thenReturn("2");
        when(response.getLabel()).thenReturn("Porn");
        when(response.getScore()).thenReturn("95");
        when(cosClient.imageAuditing(any(ImageAuditingRequest.class))).thenReturn(response);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.moderate(COS_URL, "avatar"));
        assertEquals(ResultCode.IMAGE_MODERATION_FAILED, ex.getCode());
        assertEquals("图片包含违规内容，请重新选择", ex.getMessage());

        verify(cosClient).deleteObject(BUCKET, OBJECT_KEY);
    }

    @Test
    @DisplayName("moderate - 审核不通过但删除失败仍抛2003异常")
    void testModerate_ResultBlockDeleteFails() {
        ImageAuditingResponse response = mock(ImageAuditingResponse.class);
        when(response.getResult()).thenReturn("2");
        when(response.getLabel()).thenReturn("Porn");
        when(response.getScore()).thenReturn("99");
        when(cosClient.imageAuditing(any(ImageAuditingRequest.class))).thenReturn(response);
        doThrow(new RuntimeException("COS delete failed"))
                .when(cosClient).deleteObject(eq(BUCKET), eq(OBJECT_KEY));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.moderate(COS_URL, "avatar"));
        assertEquals(ResultCode.IMAGE_MODERATION_FAILED, ex.getCode());
    }

    @Test
    @DisplayName("moderate - CI API异常抛2004异常")
    void testModerate_CiApiException() {
        when(cosClient.imageAuditing(any(ImageAuditingRequest.class)))
                .thenThrow(new RuntimeException("CI service unavailable"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> moderationService.moderate(COS_URL, "avatar"));
        assertEquals(ResultCode.IMAGE_UPLOAD_ERROR, ex.getCode());
    }
}
