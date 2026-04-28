package flcr.backend.recipe;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class RecipeMapperTest {

    @Autowired
    private RecipeMapper recipeMapper;

    @Test
    @DisplayName("测试插入菜谱")
    void testInsert() {
        Recipe recipe = buildRecipe("测试菜谱");
        int result = recipeMapper.insert(recipe);

        assertEquals(1, result);
        assertNotNull(recipe.getId());
    }

    @Test
    @DisplayName("测试根据ID查询")
    void testSelectById() {
        Recipe recipe = buildRecipe("查询测试");
        recipeMapper.insert(recipe);

        Recipe found = recipeMapper.selectById(recipe.getId());
        assertNotNull(found);
        assertEquals("查询测试", found.getName());
    }

    @Test
    @DisplayName("测试按分类查询")
    void testSelectByCategory() {
        Recipe r1 = buildRecipe("家常测试1");
        r1.setCategory("家常菜");
        recipeMapper.insert(r1);

        Recipe r2 = buildRecipe("家常测试2");
        r2.setCategory("家常菜");
        recipeMapper.insert(r2);

        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipe::getCategory, "家常菜");
        List<Recipe> list = recipeMapper.selectList(wrapper);

        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("测试更新")
    void testUpdate() {
        Recipe recipe = buildRecipe("原始名称");
        recipeMapper.insert(recipe);

        recipe.setName("更新后");
        recipe.setUpdatedAt(LocalDateTime.now());
        recipeMapper.updateById(recipe);

        Recipe updated = recipeMapper.selectById(recipe.getId());
        assertEquals("更新后", updated.getName());
    }

    @Test
    @DisplayName("测试删除")
    void testDelete() {
        Recipe recipe = buildRecipe("待删除");
        recipeMapper.insert(recipe);

        recipeMapper.deleteById(recipe.getId());
        assertNull(recipeMapper.selectById(recipe.getId()));
    }

    private Recipe buildRecipe(String name) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setCover("/uploads/test.jpg");
        recipe.setAuthorId(1L);
        recipe.setCategory("家常菜");
        recipe.setCookTime(30);
        recipe.setDifficulty(1);
        recipe.setCalories(300);
        recipe.setLikeCount(0);
        recipe.setCollectionCount(0);
        recipe.setCommentCount(0);
        recipe.setViewCount(0);
        recipe.setSource(2);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        return recipe;
    }
}
