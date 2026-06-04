package flcr.backend.ingredient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.ingredient.entity.CommonIngredient;
import flcr.backend.ingredient.mapper.CommonIngredientMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
@DisplayName("常用食材Mapper测试")
class CommonIngredientMapperTest {

    @Autowired
    private CommonIngredientMapper commonIngredientMapper;

    @Test
    @DisplayName("测试查询所有常用食材")
    void testSelectAll_ReturnsAll() {
        List<CommonIngredient> all = commonIngredientMapper.selectList(null);

        assertNotNull(all);
        assertFalse(all.isEmpty());
    }

    @Test
    @DisplayName("测试按分类查询常用食材")
    void testSelectByCategory_ReturnsByCategory() {
        LambdaQueryWrapper<CommonIngredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommonIngredient::getCategory, "蔬菜");

        List<CommonIngredient> vegetables = commonIngredientMapper.selectList(wrapper);

        assertNotNull(vegetables);
        assertFalse(vegetables.isEmpty());
        for (CommonIngredient ci : vegetables) {
            assertEquals("蔬菜", ci.getCategory());
        }
    }

    @Test
    @DisplayName("测试查询调味品分类")
    void testSelectCondiments_FiltersByCondiment() {
        LambdaQueryWrapper<CommonIngredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommonIngredient::getCategory, "调味品");

        List<CommonIngredient> condiments = commonIngredientMapper.selectList(wrapper);

        assertNotNull(condiments);
        assertFalse(condiments.isEmpty());
        for (CommonIngredient ci : condiments) {
            assertEquals("调味品", ci.getCategory());
            assertNotNull(ci.getName());
            assertNotNull(ci.getDefaultUnit());
        }
    }

    @Test
    @DisplayName("测试常用食材字段完整性")
    void testFieldIntegrity_AllFieldsNotNull() {
        LambdaQueryWrapper<CommonIngredient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommonIngredient::getCategory, "肉类");

        List<CommonIngredient> meats = commonIngredientMapper.selectList(wrapper);

        for (CommonIngredient ci : meats) {
            assertNotNull(ci.getId());
            assertNotNull(ci.getCategory());
            assertNotNull(ci.getName());
            assertNotNull(ci.getDefaultUnit());
            assertFalse(ci.getName().isEmpty());
            assertFalse(ci.getDefaultUnit().isEmpty());
        }
    }
}
