package flcr.backend.common.aop;

import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthAspectTest {

    @Mock private JwtTokenUtil jwtTokenUtil;
    @InjectMocks private AuthAspect authAspect;

    @Mock private HttpServletRequest mockRequest;
    @Mock private ProceedingJoinPoint mockJoinPoint;
    @Mock private RequireAuth mockRequireAuth;

    private static final String VALID_JWT = "eyJhbGci.valid.jwt";

    @BeforeEach
    void setUp() {
        when(mockRequireAuth.required()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("required=true且无token抛异常")
    void testRequiredAuth_NoToken() {
        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> callAuthAspect());
        assertEquals(ResultCode.USER_NOT_EXIST, ex.getCode());
        assertNull(UserContext.getUserId());
    }

    @Test
    @DisplayName("token验证通过设置UserContext并放行")
    void testAuthenticate_Success() throws Throwable {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtTokenUtil.validateToken(VALID_JWT)).thenReturn(true);
        when(jwtTokenUtil.getUserIdFromToken(VALID_JWT)).thenReturn(1001L);
        when(mockJoinPoint.proceed()).thenReturn("result");

        assertEquals("result", callAuthAspect());
    }

    @Test
    @DisplayName("最后UserContext被清除")
    void testAuthenticate_UserContextCleared() throws Throwable {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtTokenUtil.validateToken(VALID_JWT)).thenReturn(true);
        when(jwtTokenUtil.getUserIdFromToken(VALID_JWT)).thenReturn(1001L);
        when(mockJoinPoint.proceed()).thenReturn("ok");

        callAuthAspect();

        assertNull(UserContext.getUserId());
    }

    @Test
    @DisplayName("token无效拋异常")
    void testAuthenticate_InvalidToken() {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(jwtTokenUtil.validateToken("invalid_token")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> callAuthAspect());
        assertEquals(ResultCode.USER_NOT_EXIST, ex.getCode());
    }

    @Test
    @DisplayName("required=false且无token放行")
    void testOptionalAuth_NoToken() throws Throwable {
        when(mockRequireAuth.required()).thenReturn(false);
        when(mockRequest.getHeader("Authorization")).thenReturn(null);
        when(mockJoinPoint.proceed()).thenReturn("pass");

        assertEquals("pass", callAuthAspect());
        assertNull(UserContext.getUserId());
    }

    @Test
    @DisplayName("Authorization头不是Bearer格式")
    void testExtractToken_NotBearer() throws Throwable {
        when(mockRequireAuth.required()).thenReturn(false);
        when(mockRequest.getHeader("Authorization")).thenReturn("Basic some_token");
        when(mockJoinPoint.proceed()).thenReturn("pass");

        assertDoesNotThrow(() -> {
            try { callAuthAspect(); } catch (Throwable e) { throw new RuntimeException(e); }
        });
        assertNull(UserContext.getUserId());
    }

    private Object callAuthAspect() throws Throwable {
        ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);
        try {
            return authAspect.authenticate(mockJoinPoint, mockRequireAuth);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
