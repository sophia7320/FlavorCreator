package flcr.backend.common.aop;

import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock private JwtTokenUtil jwtTokenUtil;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HandlerMethod handlerMethod;

    private AuthInterceptor interceptor;

    private static final String VALID_JWT = "eyJhbGci.valid.jwt";
    private static final Long USER_ID = 1001L;

    @BeforeEach
    void setUp() {
        interceptor = new AuthInterceptor(jwtTokenUtil);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("公开接口无Token直接放行")
    void testPublic_NoToken() throws Exception {
        when(handlerMethod.hasMethodAnnotation(Public.class)).thenReturn(true);
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertNull(UserContext.getUserId());
    }

    @Test
    @DisplayName("公开接口有有效Token注入UserContext")
    void testPublic_WithValidToken() throws Exception {
        when(handlerMethod.hasMethodAnnotation(Public.class)).thenReturn(true);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtTokenUtil.getUserIdFromToken(VALID_JWT)).thenReturn(USER_ID);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertEquals(USER_ID, UserContext.getUserId());
    }

    @Test
    @DisplayName("公开接口有过期Token放行且不注入")
    void testPublic_WithExpiredToken() throws Exception {
        when(handlerMethod.hasMethodAnnotation(Public.class)).thenReturn(true);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtTokenUtil.getUserIdFromToken(VALID_JWT)).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertNull(UserContext.getUserId());
    }

    @Test
    @DisplayName("需认证接口无Token抛401")
    void testRequired_NoToken() {
        when(handlerMethod.hasMethodAnnotation(Public.class)).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/user/info");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, response, handlerMethod));
        assertEquals(ResultCode.USER_NOT_EXIST, ex.getCode());
    }

    @Test
    @DisplayName("需认证接口Token无效抛401")
    void testRequired_InvalidToken() {
        when(handlerMethod.hasMethodAnnotation(Public.class)).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtTokenUtil.getUserIdFromToken(VALID_JWT)).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/user/info");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, response, handlerMethod));
        assertEquals(ResultCode.USER_NOT_EXIST, ex.getCode());
    }

    @Test
    @DisplayName("需认证接口Token有效放行并注入")
    void testRequired_ValidToken() throws Exception {
        when(handlerMethod.hasMethodAnnotation(Public.class)).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtTokenUtil.getUserIdFromToken(VALID_JWT)).thenReturn(USER_ID);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertEquals(USER_ID, UserContext.getUserId());
    }

    @Test
    @DisplayName("afterCompletion清除UserContext")
    void testAfterCompletion() {
        UserContext.setUserId(USER_ID);
        interceptor.afterCompletion(request, response, handlerMethod, null);
        assertNull(UserContext.getUserId());
    }
}
