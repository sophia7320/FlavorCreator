package flcr.backend.ingredient.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaSubscribeService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.ingredient.cache.CachedIngredient;
import flcr.backend.ingredient.cache.IngredientHeapCache;
import flcr.backend.ingredient.entity.Ingredient;
import flcr.backend.ingredient.mapper.IngredientMapper;
import me.chanjar.weixin.common.error.WxErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("食材定时任务服务测试")
class IngredientSchedulerServiceTest {

    @Mock private IngredientMapper ingredientMapper;
    @Mock private IngredientHeapCache heapCache;
    @Mock private UserMapper userMapper;
    @Mock private ObjectMapper objectMapper;
    @Mock private WxMaService wxMaService;

    @InjectMocks
    private IngredientSchedulerService schedulerService;

    private Ingredient buildIngredient(Long id, Long userId, String name, LocalDate expireDate, Boolean readed) {
        Ingredient ing = new Ingredient();
        ing.setId(id);
        ing.setUserId(userId);
        ing.setName(name);
        ing.setExpireDate(expireDate);
        ing.setReaded(readed);
        return ing;
    }

    @Test
    @DisplayName("rebuildAll全量刷新成功")
    void testRebuildAll_Success() {
        Ingredient ing1 = buildIngredient(1L, 1001L, "牛奶",
                LocalDate.now().plusDays(2), true);
        Ingredient ing2 = buildIngredient(2L, 1002L, "鸡蛋",
                LocalDate.now().plusDays(10), false);

        when(ingredientMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(ing1, ing2));

        assertDoesNotThrow(() -> schedulerService.rebuildAll());

        verify(heapCache).rebuildAll(anyList());
    }

    @Test
    @DisplayName("rebuildAll数据库异常不抛出")
    void testRebuildAll_DbException() {
        when(ingredientMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() -> schedulerService.rebuildAll());

        verify(heapCache, never()).rebuildAll(anyList());
    }

    @Test
    @DisplayName("dailyMaintenance执行全部步骤")
    void testDailyMaintenance_AllSteps() throws WxErrorException {
        ReflectionTestUtils.setField(schedulerService, "notificationEnabled", true);
        ReflectionTestUtils.setField(schedulerService, "wxTemplateId", "test-template");
        ReflectionTestUtils.setField(schedulerService, "cacheType", "memory");

        doNothing().when(heapCache).dailyMigrate();
        doNothing().when(heapCache).cleanupExpired();

        CachedIngredient urgent = CachedIngredient.builder()
                .id(1L).userId(1001L).name("牛奶")
                .expireDate(LocalDate.now().plusDays(2))
                .readed(false).status(1).build();
        when(heapCache.peekUrgent(1000)).thenReturn(List.of(urgent));
        when(heapCache.peekExpired(1000)).thenReturn(Collections.emptyList());

        User user = new User();
        user.setId(1001L);
        user.setOpenid("mock-openid-123");
        when(userMapper.selectById(1001L)).thenReturn(user);

        WxMaSubscribeService subscribeService = mock(WxMaSubscribeService.class);
        when(wxMaService.getSubscribeService()).thenReturn(subscribeService);

        assertDoesNotThrow(() -> schedulerService.dailyMaintenance());

        verify(heapCache).dailyMigrate();
        verify(heapCache).cleanupExpired();
        verify(subscribeService).sendSubscribeMsg(any(WxMaSubscribeMessage.class));
    }

    @Test
    @DisplayName("通知禁用时跳过推送")
    void testPushNotifications_Disabled() {
        ReflectionTestUtils.setField(schedulerService, "notificationEnabled", false);

        doNothing().when(heapCache).dailyMigrate();
        doNothing().when(heapCache).cleanupExpired();

        assertDoesNotThrow(() -> schedulerService.dailyMaintenance());

        verify(heapCache).dailyMigrate();
        verify(heapCache).cleanupExpired();
        verifyNoInteractions(wxMaService);
    }

    @Test
    @DisplayName("用户无openid时跳过推送")
    void testPushNotifications_NoOpenid() {
        ReflectionTestUtils.setField(schedulerService, "notificationEnabled", true);
        ReflectionTestUtils.setField(schedulerService, "wxTemplateId", "test-template");
        ReflectionTestUtils.setField(schedulerService, "cacheType", "memory");

        doNothing().when(heapCache).dailyMigrate();
        doNothing().when(heapCache).cleanupExpired();

        CachedIngredient urgent = CachedIngredient.builder()
                .id(1L).userId(999L).name("鸡蛋")
                .expireDate(LocalDate.now().plusDays(1))
                .readed(false).status(1).build();
        when(heapCache.peekUrgent(1000)).thenReturn(List.of(urgent));
        when(heapCache.peekExpired(1000)).thenReturn(Collections.emptyList());

        when(userMapper.selectById(999L)).thenReturn(null);

        assertDoesNotThrow(() -> schedulerService.dailyMaintenance());

        verify(heapCache).dailyMigrate();
        verify(heapCache).cleanupExpired();
        verifyNoInteractions(wxMaService);
    }

    @Test
    @DisplayName("推送单个用户流程正常完成")
    void testPushNotifications_SingleVsMulti() throws WxErrorException {
        ReflectionTestUtils.setField(schedulerService, "notificationEnabled", true);
        ReflectionTestUtils.setField(schedulerService, "wxTemplateId", "test-template");
        ReflectionTestUtils.setField(schedulerService, "cacheType", "memory");

        doNothing().when(heapCache).dailyMigrate();
        doNothing().when(heapCache).cleanupExpired();

        CachedIngredient urgent = CachedIngredient.builder()
                .id(1L).userId(1001L).name("牛奶")
                .expireDate(LocalDate.now().plusDays(2))
                .readed(false).status(1).build();
        when(heapCache.peekUrgent(1000)).thenReturn(List.of(urgent));
        when(heapCache.peekExpired(1000)).thenReturn(Collections.emptyList());

        User user = new User();
        user.setId(1001L);
        user.setOpenid("mock-openid-123");
        when(userMapper.selectById(1001L)).thenReturn(user);

        WxMaSubscribeService subscribeService = mock(WxMaSubscribeService.class);
        when(wxMaService.getSubscribeService()).thenReturn(subscribeService);

        assertDoesNotThrow(() -> schedulerService.dailyMaintenance());

        verify(heapCache).dailyMigrate();
        verify(heapCache).cleanupExpired();
        verify(subscribeService).sendSubscribeMsg(any(WxMaSubscribeMessage.class));
    }
}
