package flcr.backend.ingredient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.ingredient.DTO.request.*;
import flcr.backend.ingredient.DTO.response.*;
import flcr.backend.ingredient.entity.CommonIngredient;
import flcr.backend.ingredient.entity.Ingredient;
import flcr.backend.ingredient.mapper.CommonIngredientMapper;
import flcr.backend.ingredient.mapper.IngredientMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceImplTest {

    @Mock private IngredientMapper ingredientMapper;
    @Mock private CommonIngredientMapper commonIngredientMapper;
    @InjectMocks private IngredientServiceImpl ingredientService;

    private static final Long USER_ID = 1001L;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("list按分类筛选返回列表")
    void testList_ByCategory() {
        IngredientListQueryDTO query = new IngredientListQueryDTO();
        query.setCategory("蔬菜");

        Ingredient tomato = buildIngredient(1L, "西红柿");
        when(ingredientMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(tomato));

        IngredientListResponseDTO result = ingredientService.list(query);

        assertEquals(1, result.getIngredients().size());
        assertEquals("西红柿", result.getIngredients().get(0).getName());
        assertEquals("normal", result.getIngredients().get(0).getStatus());
    }

    @Test
    @DisplayName("list空结果返回空列表和summary")
    void testList_Empty() {
        IngredientListQueryDTO query = new IngredientListQueryDTO();
        when(ingredientMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        IngredientListResponseDTO result = ingredientService.list(query);

        assertEquals(0, result.getIngredients().size());
        assertEquals(0, result.getSummary().getTotalCount());
    }

    @Test
    @DisplayName("add成功返回id")
    void testAdd_Success() {
        IngredientAddRequestDTO request = new IngredientAddRequestDTO();
        request.setName("鸡蛋");
        request.setQuantity(new BigDecimal("6"));
        request.setUnit("个");
        request.setCategory("蛋类");

        when(ingredientMapper.insert(any(Ingredient.class))).thenAnswer(inv -> {
            Ingredient ing = inv.getArgument(0);
            ing.setId(1L);
            return 1;
        });

        Long id = ingredientService.add(request);
        assertEquals(1L, id);
    }

    @Test
    @DisplayName("update不存在抛异常")
    void testUpdate_NotFound() {
        when(ingredientMapper.selectById(1L)).thenReturn(null);

        IngredientUpdateRequestDTO request = new IngredientUpdateRequestDTO();
        BusinessException ex = assertThrows(BusinessException.class, () -> ingredientService.update(1L, request));
        assertEquals(ResultCode.RESOURCE_NOT_EXIST, ex.getCode());
    }

    @Test
    @DisplayName("update非本人抛异常")
    void testUpdate_NotOwner() {
        Ingredient ingredient = buildIngredient(1L, "鸡蛋");
        ingredient.setUserId(999L); // 不是当前用户
        when(ingredientMapper.selectById(1L)).thenReturn(ingredient);

        IngredientUpdateRequestDTO request = new IngredientUpdateRequestDTO();
        BusinessException ex = assertThrows(BusinessException.class, () -> ingredientService.update(1L, request));
        assertEquals(ResultCode.PERMISSION_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("update成功")
    void testUpdate_Success() {
        Ingredient ingredient = buildIngredient(1L, "鸡蛋");
        ingredient.setUserId(USER_ID);
        when(ingredientMapper.selectById(1L)).thenReturn(ingredient);
        when(ingredientMapper.updateById(any(Ingredient.class))).thenReturn(1);

        IngredientUpdateRequestDTO request = new IngredientUpdateRequestDTO();
        request.setName("鸭蛋");
        assertDoesNotThrow(() -> ingredientService.update(1L, request));
        assertEquals("鸭蛋", ingredient.getName());
    }

    @Test
    @DisplayName("delete不存在抛异常")
    void testDelete_NotFound() {
        when(ingredientMapper.selectById(1L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> ingredientService.delete(1L));
        assertEquals(ResultCode.RESOURCE_NOT_EXIST, ex.getCode());
    }

    @Test
    @DisplayName("delete非本人抛异常")
    void testDelete_NotOwner() {
        Ingredient ingredient = buildIngredient(1L, "鸡蛋");
        ingredient.setUserId(999L);
        when(ingredientMapper.selectById(1L)).thenReturn(ingredient);

        BusinessException ex = assertThrows(BusinessException.class, () -> ingredientService.delete(1L));
        assertEquals(ResultCode.PERMISSION_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("batchAdd成功返回ids")
    void testBatchAdd_Success() {
        IngredientBatchAddRequestDTO.BatchItem item1 = new IngredientBatchAddRequestDTO.BatchItem();
        item1.setName("鸡蛋"); item1.setQuantity(new BigDecimal("6")); item1.setUnit("个");
        IngredientBatchAddRequestDTO.BatchItem item2 = new IngredientBatchAddRequestDTO.BatchItem();
        item2.setName("牛奶"); item2.setQuantity(new BigDecimal("1")); item2.setUnit("盒");

        IngredientBatchAddRequestDTO request = new IngredientBatchAddRequestDTO();
        request.setIngredients(List.of(item1, item2));

        when(ingredientMapper.insert(any(Ingredient.class))).thenAnswer(inv -> {
            Ingredient ing = inv.getArgument(0);
            ing.setId((long) (ing.getName().hashCode() % 100 + 1));
            return 1;
        });

        List<Long> ids = ingredientService.batchAdd(request);
        assertEquals(2, ids.size());
    }

    @Test
    @DisplayName("expiringNotice区分临期和过期")
    void testExpiringNotice() {
        Ingredient expiring = buildIngredient(1L, "牛奶");
        expiring.setExpireDate(LocalDate.now().plusDays(2)); // 临期
        Ingredient expired = buildIngredient(2L, "青菜");
        expired.setExpireDate(LocalDate.now().minusDays(1)); // 过期

        when(ingredientMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(expiring, expired));

        ExpiringNoticeResponseDTO result = ingredientService.expiringNotice();

        assertEquals(1, result.getExpiring().size());
        assertEquals(1, result.getExpired().size());
        assertEquals("牛奶", result.getExpiring().get(0).getName());
        assertEquals("青菜", result.getExpired().get(0).getName());
    }

    @Test
    @DisplayName("commonList返回分类分组")
    void testCommonList() {
        CommonIngredient tomato = new CommonIngredient();
        tomato.setCategory("蔬菜"); tomato.setName("西红柿"); tomato.setDefaultUnit("个");
        CommonIngredient pork = new CommonIngredient();
        pork.setCategory("肉类"); pork.setName("猪肉"); pork.setDefaultUnit("克");

        when(commonIngredientMapper.selectList(null)).thenReturn(List.of(tomato, pork));

        CommonIngredientResponseDTO result = ingredientService.commonList();

        assertEquals(2, result.getCategories().size());
    }

    private Ingredient buildIngredient(Long id, String name) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(id);
        ingredient.setName(name);
        ingredient.setUserId(USER_ID);
        ingredient.setQuantity(new BigDecimal("1"));
        ingredient.setUnit("个");
        ingredient.setCategory("其他");
        ingredient.setExpireDate(LocalDate.now().plusDays(14));
        return ingredient;
    }
}
