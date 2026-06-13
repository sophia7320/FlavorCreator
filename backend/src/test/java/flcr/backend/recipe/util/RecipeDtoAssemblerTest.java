package flcr.backend.recipe.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeDtoAssembler DTO 组装器")
class RecipeDtoAssemblerTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RecipeDtoAssembler assembler;

    private static User createUser(Long id, String nickname, String avatar) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        return user;
    }

    private static Recipe createFullRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("番茄炒蛋");
        recipe.setCover("http://example.com/cover.jpg");
        recipe.setAuthorId(100L);
        recipe.setCookTime("15分钟");
        recipe.setDifficulty(1);
        recipe.setDesc("一道经典家常菜");
        recipe.setCalories(200);
        recipe.setTags("[\"家常\",\"快手\"]");
        recipe.setLikeCount(10);
        recipe.setCollectionCount(20);
        recipe.setCommentCount(5);
        recipe.setViewCount(100);
        recipe.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 30, 0));
        recipe.setImages(List.of("http://example.com/img1.jpg"));
        recipe.setIngredients("[{\"name\":\"番茄\",\"quantity\":2.0,\"unit\":\"个\"}]");
        recipe.setSteps("[{\"order\":1,\"description\":\"切番茄\"}]");
        recipe.setTips("小贴士");
        recipe.setCategory("家常菜");
        return recipe;
    }

    @Nested
    @DisplayName("convertToListItemDTO() 列表项 DTO 转换")
    class ConvertToListItemDTO {

        @Test
        @DisplayName("完整菜谱 → 所有字段正确映射")
        void testConvertToListItemDTO_FullRecipe() throws Exception {
            Recipe recipe = createFullRecipe();
            User author = createUser(100L, "厨神", "http://example.com/avatar.jpg");
            String[] expectedTags = {"家常", "快手"};

            when(userMapper.selectById(100L)).thenReturn(author);
            when(objectMapper.readValue(recipe.getTags(), String[].class)).thenReturn(expectedTags);

            RecipeListItemResponseDTO dto = assembler.convertToListItemDTO(recipe);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("番茄炒蛋", dto.getName());
            assertEquals("http://example.com/cover.jpg", dto.getCover());

            assertNotNull(dto.getAuthor());
            assertEquals(100L, dto.getAuthor().getId());
            assertEquals("厨神", dto.getAuthor().getNickname());
            assertEquals("http://example.com/avatar.jpg", dto.getAuthor().getAvatar());

            assertEquals("15分钟", dto.getCookTime());
            assertEquals("简单", dto.getDifficulty());
            assertEquals("一道经典家常菜", dto.getDesc());
            assertEquals(200, dto.getCalories());
            assertArrayEquals(expectedTags, dto.getTags());

            assertNotNull(dto.getStats());
            assertEquals(10, dto.getStats().getLikes());
            assertEquals(20, dto.getStats().getCollections());
            assertEquals(5, dto.getStats().getComments());
            assertEquals(100, dto.getStats().getViews());

            assertEquals("2026-01-01T10:30:00", dto.getCreatedAt());
        }

        @Test
        @DisplayName("authorId 为 null → 作者显示未知用户和空头像")
        void testConvertToListItemDTO_NullAuthorId() throws Exception {
            Recipe recipe = createFullRecipe();
            recipe.setAuthorId(null);

            when(objectMapper.readValue(recipe.getTags(), String[].class)).thenReturn(new String[]{"家常"});

            RecipeListItemResponseDTO dto = assembler.convertToListItemDTO(recipe);

            assertNotNull(dto.getAuthor());
            assertNull(dto.getAuthor().getId());
            assertEquals("未知用户", dto.getAuthor().getNickname());
            assertEquals("", dto.getAuthor().getAvatar());
        }

        @Test
        @DisplayName("tags 为 null → 返回空数组，无异常")
        void testConvertToListItemDTO_NullTags() {
            Recipe recipe = createFullRecipe();
            recipe.setTags(null);

            when(userMapper.selectById(any())).thenReturn(createUser(100L, "厨神", "avatar.jpg"));

            RecipeListItemResponseDTO dto = assembler.convertToListItemDTO(recipe);

            assertNotNull(dto.getTags());
            assertEquals(0, dto.getTags().length);
        }

        @Test
        @DisplayName("JSON 解析异常 → 抛出 BusinessException")
        void testConvertToListItemDTO_JsonParseFailure() throws Exception {
            Recipe recipe = createFullRecipe();
            recipe.setTags("invalid json");

            when(userMapper.selectById(any())).thenReturn(createUser(100L, "厨神", "avatar.jpg"));
            when(objectMapper.readValue(recipe.getTags(), String[].class))
                    .thenThrow(new JsonProcessingException("parse error") {});

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> assembler.convertToListItemDTO(recipe));
            assertEquals(ResultCode.SYSTEM_ERROR, ex.getCode());
            assertEquals("JSON解析失败", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("convertToDetailDTO() 详情 DTO 转换")
    class ConvertToDetailDTO {

        @Test
        @DisplayName("完整菜谱 → 所有字段正确映射，含 category")
        void testConvertToDetailDTO_FullRecipe() throws Exception {
            Recipe recipe = createFullRecipe();
            User author = createUser(100L, "厨神", "http://example.com/avatar.jpg");

            String ingredientsJson = recipe.getIngredients();
            String stepsJson = recipe.getSteps();
            String tagsJson = recipe.getTags();
            List<RecipeDetailResponseDTO.IngredientItem> expectedIngredients = List.of(
                    RecipeDetailResponseDTO.IngredientItem.builder().name("番茄").quantity(2.0).unit("个").build()
            );
            List<RecipeDetailResponseDTO.StepItem> expectedSteps = List.of(
                    RecipeDetailResponseDTO.StepItem.builder().order(1).description("切番茄").build()
            );
            String[] expectedTags = {"家常", "快手"};

            when(userMapper.selectById(100L)).thenReturn(author);
            when(objectMapper.readValue(eq(ingredientsJson), any(TypeReference.class))).thenReturn(expectedIngredients);
            when(objectMapper.readValue(eq(stepsJson), any(TypeReference.class))).thenReturn(expectedSteps);
            when(objectMapper.readValue(eq(tagsJson), eq(String[].class))).thenReturn(expectedTags);

            RecipeDetailResponseDTO dto = assembler.convertToDetailDTO(recipe);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("番茄炒蛋", dto.getName());
            assertEquals("http://example.com/cover.jpg", dto.getCover());

            assertNotNull(dto.getImages());
            assertEquals(1, dto.getImages().size());
            assertEquals("http://example.com/img1.jpg", dto.getImages().get(0));

            assertNotNull(dto.getAuthor());
            assertEquals(100L, dto.getAuthor().getId());
            assertEquals("厨神", dto.getAuthor().getNickname());
            assertEquals("http://example.com/avatar.jpg", dto.getAuthor().getAvatar());

            assertNotNull(dto.getIngredients());
            assertEquals(1, dto.getIngredients().size());
            assertEquals("番茄", dto.getIngredients().get(0).getName());
            assertEquals(2.0, dto.getIngredients().get(0).getQuantity());
            assertEquals("个", dto.getIngredients().get(0).getUnit());

            assertNotNull(dto.getSteps());
            assertEquals(1, dto.getSteps().size());
            assertEquals(1, dto.getSteps().get(0).getOrder());
            assertEquals("切番茄", dto.getSteps().get(0).getDescription());

            assertArrayEquals(expectedTags, dto.getTags());
            assertEquals("家常菜", dto.getCategory());
            assertEquals("小贴士", dto.getTips());
            assertEquals("15分钟", dto.getCookTime());
            assertEquals("简单", dto.getDifficulty());
            assertEquals(200, dto.getCalories());
            assertEquals("一道经典家常菜", dto.getDesc());

            assertNotNull(dto.getStats());
            assertEquals(10, dto.getStats().getLikes());
            assertEquals(20, dto.getStats().getCollections());
            assertEquals(5, dto.getStats().getComments());
            assertEquals(100, dto.getStats().getViews());
        }

        @Test
        @DisplayName("ingredients/steps/tags 均为 null → 返回空集合/空数组")
        void testConvertToDetailDTO_NullJsonFields() {
            Recipe recipe = createFullRecipe();
            recipe.setIngredients(null);
            recipe.setSteps(null);
            recipe.setTags(null);

            when(userMapper.selectById(any())).thenReturn(createUser(100L, "厨神", "avatar.jpg"));

            RecipeDetailResponseDTO dto = assembler.convertToDetailDTO(recipe);

            assertNotNull(dto.getIngredients());
            assertTrue(dto.getIngredients().isEmpty());
            assertNotNull(dto.getSteps());
            assertTrue(dto.getSteps().isEmpty());
            assertNotNull(dto.getTags());
            assertEquals(0, dto.getTags().length);
        }

        @Test
        @DisplayName("images 为 null → 返回空列表")
        void testConvertToDetailDTO_NullImages() {
            Recipe recipe = createFullRecipe();
            recipe.setImages(null);
            recipe.setIngredients(null);
            recipe.setSteps(null);
            recipe.setTags(null);

            when(userMapper.selectById(any())).thenReturn(createUser(100L, "厨神", "avatar.jpg"));

            RecipeDetailResponseDTO dto = assembler.convertToDetailDTO(recipe);

            assertNotNull(dto.getImages());
            assertTrue(dto.getImages().isEmpty());
        }

        @Test
        @DisplayName("作者存在 → 作者信息正确填充")
        void testConvertToDetailDTO_AuthorFound() throws Exception {
            Recipe recipe = createFullRecipe();
            User author = createUser(100L, "大厨", "http://example.com/avatar.jpg");

            when(userMapper.selectById(100L)).thenReturn(author);
            when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(new ArrayList<>());
            when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[0]);

            RecipeDetailResponseDTO dto = assembler.convertToDetailDTO(recipe);

            assertEquals(100L, dto.getAuthor().getId());
            assertEquals("大厨", dto.getAuthor().getNickname());
            assertEquals("http://example.com/avatar.jpg", dto.getAuthor().getAvatar());
        }

        @Test
        @DisplayName("作者为 null → 显示未知用户")
        void testConvertToDetailDTO_AuthorNull() throws Exception {
            Recipe recipe = createFullRecipe();

            when(userMapper.selectById(100L)).thenReturn(null);
            when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(new ArrayList<>());
            when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[0]);

            RecipeDetailResponseDTO dto = assembler.convertToDetailDTO(recipe);

            assertNull(dto.getAuthor().getId());
            assertEquals("未知用户", dto.getAuthor().getNickname());
            assertEquals("", dto.getAuthor().getAvatar());
        }

        @Test
        @DisplayName("JSON 解析异常 → 抛出 BusinessException")
        void testConvertToDetailDTO_JsonParseFailure() throws Exception {
            Recipe recipe = createFullRecipe();
            recipe.setIngredients("invalid json");

            when(userMapper.selectById(any())).thenReturn(createUser(100L, "厨神", "avatar.jpg"));
            when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                    .thenThrow(new JsonProcessingException("parse error") {});

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> assembler.convertToDetailDTO(recipe));
            assertEquals(ResultCode.SYSTEM_ERROR, ex.getCode());
            assertEquals("JSON解析失败", ex.getMessage());
        }
    }
}
