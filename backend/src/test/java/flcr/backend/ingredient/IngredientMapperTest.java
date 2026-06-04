package flcr.backend.ingredient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.ingredient.entity.Ingredient;
import flcr.backend.ingredient.mapper.IngredientMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@DisplayName("食材Mapper测试")
class IngredientMapperTest {

    @Autowired
    private IngredientMapper ingredientMapper;

    @Test
    @DisplayName("测试插入食材")
    void testInsert_Success() {
        Ingredient ingredient = buildIngredient("鸡蛋", "蛋类", 6, "个", LocalDate.now().plusDays(14));
        int result = ingredientMapper.insert(ingredient);

        assertEquals(1, result);
        assertNotNull(ingredient.getId());
        assertTrue(ingredient.getId() > 0);
    }

    @Test
    @DisplayName("测试根据ID查询食材")
    void testSelectById_Success() {
        Ingredient ingredient = buildIngredient("牛奶", "乳制品", 1, "盒", LocalDate.now().plusDays(5));
        ingredientMapper.insert(ingredient);

        Ingredient found = ingredientMapper.selectById(ingredient.getId());

        assertNotNull(found);
        assertEquals("牛奶", found.getName());
        assertEquals("乳制品", found.getCategory());
    }

    @Test
    @DisplayName("测试更新食材")
    void testUpdateById_Success() {
        Ingredient ingredient = buildIngredient("西红柿", "蔬菜", 3, "个", LocalDate.now().plusDays(7));
        ingredientMapper.insert(ingredient);

        ingredient.setQuantity(new BigDecimal("2"));
        ingredient.setExpireDate(LocalDate.now().plusDays(3));
        ingredient.setUpdatedAt(LocalDateTime.now());
        int result = ingredientMapper.updateById(ingredient);

        assertEquals(1, result);

        Ingredient updated = ingredientMapper.selectById(ingredient.getId());
        assertEquals(0, new BigDecimal("2").compareTo(updated.getQuantity()));
        assertEquals(LocalDate.now().plusDays(3), updated.getExpireDate());
    }

    @Test
    @DisplayName("测试删除食材")
    void testDeleteById_Success() {
        Ingredient ingredient = buildIngredient("待删除食材", "其他", 1, "份", null);
        ingredientMapper.insert(ingredient);
        Long id = ingredient.getId();

        assertNotNull(ingredientMapper.selectById(id));

        int result = ingredientMapper.deleteById(id);
        assertEquals(1, result);

        assertNull(ingredientMapper.selectById(id));
    }

    @Test
    @DisplayName("测试批量插入食材")
    void testBatchInsert_Success() {
        Long userId = 10001L;
        for (int i = 0; i < 3; i++) {
            Ingredient ingredient = buildIngredient("批量食材" + (i + 1), "蔬菜", i + 1, "个", LocalDate.now().plusDays(i + 5));
            ingredient.setUserId(userId);
            ingredientMapper.insert(ingredient);
        }

        LambdaQueryWrapper<Ingredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ingredient::getUserId, userId)
                .likeRight(Ingredient::getName, "批量食材");
        List<Ingredient> ingredients = ingredientMapper.selectList(wrapper);

        assertEquals(3, ingredients.size());
    }

    @Test
    @DisplayName("测试根据用户ID查询食材")
    void testSelectByUserId_ReturnsUserIngredients() {
        Long userId = 20001L;
        Ingredient i1 = buildIngredient("苹果", "水果", 5, "个", LocalDate.now().plusDays(10));
        i1.setUserId(userId);
        ingredientMapper.insert(i1);

        Ingredient i2 = buildIngredient("香蕉", "水果", 3, "根", LocalDate.now().plusDays(5));
        i2.setUserId(userId);
        ingredientMapper.insert(i2);

        LambdaQueryWrapper<Ingredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ingredient::getUserId, userId);
        List<Ingredient> ingredients = ingredientMapper.selectList(wrapper);

        assertEquals(2, ingredients.size());
    }

    @Test
    @DisplayName("测试根据分类查询食材")
    void testSelectByCategory_ReturnsByCategory() {
        Long userId = 30001L;
        Ingredient i1 = buildIngredient("猪肉", "肉类", 500, "克", LocalDate.now().plusDays(3));
        i1.setUserId(userId);
        ingredientMapper.insert(i1);

        Ingredient i2 = buildIngredient("青菜", "蔬菜", 2, "颗", LocalDate.now().plusDays(5));
        i2.setUserId(userId);
        ingredientMapper.insert(i2);

        LambdaQueryWrapper<Ingredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ingredient::getUserId, userId)
                .eq(Ingredient::getCategory, "肉类");
        List<Ingredient> ingredients = ingredientMapper.selectList(wrapper);

        assertEquals(1, ingredients.size());
        assertEquals("猪肉", ingredients.get(0).getName());
    }

    @Test
    @DisplayName("测试按保质期升序排序")
    void testOrderByExpireDateAsc_SortsByExpiry() {
        Long userId = 40001L;
        Ingredient i1 = buildIngredient("临期食材", "乳制品", 1, "盒", LocalDate.now().plusDays(1));
        i1.setUserId(userId);
        ingredientMapper.insert(i1);

        Ingredient i2 = buildIngredient("远期食材", "谷物", 1, "袋", LocalDate.now().plusDays(30));
        i2.setUserId(userId);
        ingredientMapper.insert(i2);

        LambdaQueryWrapper<Ingredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ingredient::getUserId, userId)
                .orderByAsc(Ingredient::getExpireDate);
        List<Ingredient> ingredients = ingredientMapper.selectList(wrapper);

        assertEquals(2, ingredients.size());
        assertEquals("临期食材", ingredients.get(0).getName());
    }

    @Test
    @DisplayName("测试计数查询")
    void testCount_ReturnsCorrectCount() {
        Long userId = 50001L;
        long before = ingredientMapper.selectCount(
                new LambdaQueryWrapper<Ingredient>().eq(Ingredient::getUserId, userId));

        Ingredient ingredient = buildIngredient("计数测试", "蔬菜", 1, "个", null);
        ingredient.setUserId(userId);
        ingredientMapper.insert(ingredient);

        long after = ingredientMapper.selectCount(
                new LambdaQueryWrapper<Ingredient>().eq(Ingredient::getUserId, userId));
        assertEquals(before + 1, after);
    }

    private Ingredient buildIngredient(String name, String category, int quantity, String unit, LocalDate expireDate) {
        Ingredient ingredient = new Ingredient();
        ingredient.setUserId(1L);
        ingredient.setName(name);
        ingredient.setCategory(category);
        ingredient.setQuantity(new BigDecimal(quantity));
        ingredient.setUnit(unit);
        ingredient.setExpireDate(expireDate);
        ingredient.setStorageCondition("冷藏");
        ingredient.setCreatedAt(LocalDateTime.now());
        ingredient.setUpdatedAt(LocalDateTime.now());
        return ingredient;
    }
}
