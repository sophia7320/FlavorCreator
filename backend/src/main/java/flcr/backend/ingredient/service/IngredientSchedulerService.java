package flcr.backend.ingredient.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.ingredient.cache.CachedIngredient;
import flcr.backend.ingredient.cache.IngredientHeapCache;
import flcr.backend.ingredient.constants.IngredientStatus;
import flcr.backend.ingredient.entity.Ingredient;
import flcr.backend.ingredient.mapper.IngredientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientSchedulerService {

    private final IngredientMapper ingredientMapper;
    private final IngredientHeapCache heapCache;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    // wxMaService is optional — may be null if WeChat is not configured
    private final WxMaService wxMaService;

    @Value("${flcr.notification.enabled:false}")
    private boolean notificationEnabled;

    @Value("${flcr.notification.wx-template-id:}")
    private String wxTemplateId;

    @Value("${flcr.cache.type:memory}")
    private String cacheType;

    @Scheduled(cron = "0 0 3 */30 * ?")
    public void rebuildAll() {
        log.info("[Scheduler] rebuildAll — 开始重建食材缓存");

        try {
            LocalDate now = LocalDate.now();
            LocalDate deadline = now.plusDays(30);

            LambdaQueryWrapper<Ingredient> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNotNull(Ingredient::getExpireDate)
                   .le(Ingredient::getExpireDate, deadline);

            List<Ingredient> ingredients = ingredientMapper.selectList(wrapper);
            log.info("[Scheduler] rebuildAll — 查询到 {} 条食材记录", ingredients.size());

            List<CachedIngredient> cached = new ArrayList<>(ingredients.size());
            for (Ingredient ing : ingredients) {
                int statusCode = IngredientStatus.compute(ing.getExpireDate()).getCode();
                CachedIngredient ci = CachedIngredient.from(ing, statusCode);
                if (ing.getReaded() != null) {
                    ci.setReaded(ing.getReaded());
                }
                cached.add(ci);
            }

            heapCache.rebuildAll(cached);
            log.info("[Scheduler] rebuildAll — 缓存重建完成，共 {} 条", cached.size());

        } catch (Exception e) {
            log.error("[Scheduler] rebuildAll — 缓存重建异常", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyMaintenance() {
        log.info("[Scheduler] dailyMaintenance — 开始日常维护");

        try {
            heapCache.dailyMigrate();
            log.info("[Scheduler] dailyMaintenance — 状态迁移完成");
        } catch (Exception e) {
            log.error("[Scheduler] dailyMaintenance — 状态迁移异常", e);
        }

        try {
            heapCache.cleanupExpired();
            log.info("[Scheduler] dailyMaintenance — 过期清理完成");
        } catch (Exception e) {
            log.error("[Scheduler] dailyMaintenance — 过期清理异常", e);
        }

        try {
            pushNotifications();
        } catch (Exception e) {
            log.error("[Scheduler] dailyMaintenance — 推送通知异常", e);
        }
    }

    private void pushNotifications() {
        if (!notificationEnabled) {
            log.debug("[Scheduler] pushNotifications — 通知未启用，跳过");
            return;
        }
        if (wxTemplateId == null || wxTemplateId.isBlank()) {
            log.warn("[Scheduler] pushNotifications — wxTemplateId 未配置，跳过");
            return;
        }
        if (wxMaService == null) {
            log.warn("[Scheduler] pushNotifications — wxMaService 不可用（微信未配置），跳过");
            return;
        }

        List<CachedIngredient> urgent = heapCache.peekUrgent(1000);
        List<CachedIngredient> expired = heapCache.peekExpired(1000);
        log.info("[Scheduler] pushNotifications — 紧急 {} 条，过期 {} 条", urgent.size(), expired.size());

        List<CachedIngredient> unread = new ArrayList<>();
        for (CachedIngredient ci : urgent) {
            if (ci.getReaded() == null || !ci.getReaded()) {
                unread.add(ci);
            }
        }
        for (CachedIngredient ci : expired) {
            if (ci.getReaded() == null || !ci.getReaded()) {
                unread.add(ci);
            }
        }

        if (unread.isEmpty()) {
            log.info("[Scheduler] pushNotifications — 无未读食材，跳过");
            return;
        }

        Map<Long, List<CachedIngredient>> grouped = unread.stream()
                .collect(Collectors.groupingBy(CachedIngredient::getUserId));

        log.info("[Scheduler] pushNotifications — 即将推送 {} 个用户", grouped.size());

        for (Map.Entry<Long, List<CachedIngredient>> entry : grouped.entrySet()) {
            Long userId = entry.getKey();
            List<CachedIngredient> items = entry.getValue();

            try {
                User user = userMapper.selectById(userId);
                if (user == null || user.getOpenid() == null || user.getOpenid().isBlank()) {
                    log.warn("[Scheduler] pushNotifications — userId={} 无 openid，跳过", userId);
                    continue;
                }

                String openid = user.getOpenid();
                CachedIngredient first = items.get(0);

                String thing1;
                if (items.size() == 1) {
                    thing1 = first.getName() != null ? first.getName() : "食材";
                } else {
                    thing1 = first.getName() != null
                            ? first.getName() + " 等" + items.size() + "个食材"
                            : items.size() + "个食材";
                }

                String date2 = first.getExpireDate() != null
                        ? first.getExpireDate().toString()
                        : "即将过期";

                String thing8 = "您有 " + items.size() + " 个食材即将过期，请及时处理";

                WxMaSubscribeMessage message = WxMaSubscribeMessage.builder()
                        .toUser(openid)
                        .templateId(wxTemplateId)
                        .page("pages/ingredient/index")
                        .data(List.of(
                                new WxMaSubscribeMessage.MsgData("thing1", thing1),
                                new WxMaSubscribeMessage.MsgData("date2", date2),
                                new WxMaSubscribeMessage.MsgData("thing8", thing8)
                        ))
                        .build();

                wxMaService.getSubscribeService().sendSubscribeMsg(message);
                log.info("[Scheduler] pushNotifications — 推送给 userId={} 成功，{} 条未读", userId, items.size());

            } catch (Exception e) {
                log.error("[Scheduler] pushNotifications — 推送给 userId={} 失败", userId, e);
            }
        }

        log.info("[Scheduler] pushNotifications — 推送完成");
    }
}
