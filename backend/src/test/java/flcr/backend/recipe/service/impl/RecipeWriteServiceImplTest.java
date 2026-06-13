package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Comment;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.CommentMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.request.CreateRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeUpdateRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.recipe.util.RecipeDtoAssembler;
import flcr.backend.recipe.util.RecipeValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
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
@DisplayName("RecipeWriteServiceImpl 单元测试")
class RecipeWriteServiceImplTest {

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private RecipeValidator recipeValidator;

    @Mock
    private RecipeDtoAssembler recipeDtoAssembler;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private RecipeWriteServiceImpl recipeWriteService;

    private static final Long USER_ID = 1001L;
    private static final Long RECIPE_ID = 1L;

    @Captor
    private ArgumentCaptor<Recipe> recipeCaptor;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== publishRecipe ====================

    @Test
    @DisplayName("发布菜谱成功，返回生成的ID")
    void testPublishRecipe_Success() {
        CreateRecipeRequestDTO request = createFullRequest();
        doAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(RECIPE_ID);
            return 1;
        }).when(recipeMapper).insert(any(Recipe.class));

        Long result = recipeWriteService.publishRecipe(request);

        assertEquals(RECIPE_ID, result);
        verify(recipeMapper).insert(any(Recipe.class));
    }

    @Test
    @DisplayName("发布菜谱时校验分类委托给RecipeValidator")
    void testPublishRecipe_CategoryValidationDelegation() {
        CreateRecipeRequestDTO request = createFullRequest();
        request.setCategory("home");
        doAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(RECIPE_ID);
            return 1;
        }).when(recipeMapper).insert(any(Recipe.class));

        recipeWriteService.publishRecipe(request);

        verify(recipeValidator).validateCategory("home");
    }

    @Test
    @DisplayName("发布菜谱时校验JSON字段委托给RecipeValidator")
    void testPublishRecipe_JsonFieldValidationDelegation() {
        CreateRecipeRequestDTO request = createFullRequest();
        request.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        request.setSteps("[{\"order\":1,\"description\":\"打鸡蛋\"}]");
        request.setTags("[\"家常\"]");
        doAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(RECIPE_ID);
            return 1;
        }).when(recipeMapper).insert(any(Recipe.class));

        recipeWriteService.publishRecipe(request);

        verify(recipeValidator).validateJsonField("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]", "ingredients");
        verify(recipeValidator).validateJsonField("[{\"order\":1,\"description\":\"打鸡蛋\"}]", "steps");
        verify(recipeValidator).validateJsonField("[\"家常\"]", "tags");
    }

    @Test
    @DisplayName("发布菜谱时所有DT O字段正确映射到Recipe实体")
    void testPublishRecipe_AllFieldsMapped() {
        CreateRecipeRequestDTO request = createFullRequest();
        List<String> imageUrls = List.of("http://example.com/img1.jpg");
        request.setImageUrls(imageUrls);
        doAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(RECIPE_ID);
            return 1;
        }).when(recipeMapper).insert(recipeCaptor.capture());

        recipeWriteService.publishRecipe(request);

        Recipe captured = recipeCaptor.getValue();
        assertEquals(request.getName(), captured.getName());
        assertEquals(request.getCoverUrl(), captured.getCover());
        assertEquals(request.getDesc(), captured.getDesc());
        assertEquals(request.getCategory(), captured.getCategory());
        assertEquals(request.getTips(), captured.getTips());
        assertEquals(request.getCookTime(), captured.getCookTime());
        assertEquals(request.getDifficulty(), captured.getDifficulty());
        assertEquals(request.getCalories(), captured.getCalories());
        assertEquals(imageUrls, captured.getImages());
        assertEquals(request.getIngredients(), captured.getIngredients());
        assertEquals(request.getSteps(), captured.getSteps());
        assertEquals(request.getTags(), captured.getTags());
    }

    @Test
    @DisplayName("发布菜谱时自动设置作者ID、来源=2、计数器归零和时间戳")
    void testPublishRecipe_AutoFields() {
        CreateRecipeRequestDTO request = createFullRequest();
        doAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(RECIPE_ID);
            return 1;
        }).when(recipeMapper).insert(recipeCaptor.capture());

        recipeWriteService.publishRecipe(request);

        Recipe captured = recipeCaptor.getValue();
        assertEquals(USER_ID, captured.getAuthorId());
        assertEquals(2, captured.getSource()); // SourceConstants.USER = 2
        assertEquals(0, captured.getLikeCount());
        assertEquals(0, captured.getCollectionCount());
        assertEquals(0, captured.getCommentCount());
        assertEquals(0, captured.getViewCount());
        assertNotNull(captured.getCreatedAt());
        assertNotNull(captured.getUpdatedAt());
    }

    // ==================== updateRecipe ====================

    @Test
    @DisplayName("更新菜谱成功，所有字段均更新")
    void testUpdateRecipe_Success() {
        Recipe existingRecipe = createExistingRecipe();
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(existingRecipe);

        RecipeUpdateRequestDTO request = createFullUpdateRequest();
        RecipeDetailResponseDTO expectedDto = RecipeDetailResponseDTO.builder().id(RECIPE_ID).name("new name").build();
        when(recipeDtoAssembler.convertToDetailDTO(any(Recipe.class))).thenReturn(expectedDto);

        RecipeDetailResponseDTO result = recipeWriteService.updateRecipe(RECIPE_ID, request);

        assertSame(expectedDto, result);
        verify(recipeMapper).updateById(recipeCaptor.capture());
        Recipe updated = recipeCaptor.getValue();

        assertEquals(request.getName(), updated.getName());
        assertEquals(request.getCoverUrl(), updated.getCover());
        assertEquals(request.getImageUrls(), updated.getImages());
        assertEquals(request.getDesc(), updated.getDesc());
        assertEquals(request.getCategory(), updated.getCategory());
        assertEquals(request.getTips(), updated.getTips());
        assertEquals(request.getCookTime(), updated.getCookTime());
        assertEquals(request.getDifficulty(), updated.getDifficulty());
        assertEquals(request.getCalories(), updated.getCalories());
        assertEquals(request.getIngredients(), updated.getIngredients());
        assertEquals(request.getSteps(), updated.getSteps());
        assertEquals(request.getTags(), updated.getTags());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    @DisplayName("部分更新菜谱，仅修改名称且不改变其他字段")
    void testUpdateRecipe_PartialUpdate() {
        Recipe existingRecipe = createExistingRecipe();
        existingRecipe.setName("original");
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(existingRecipe);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("new name");
        when(recipeDtoAssembler.convertToDetailDTO(any(Recipe.class)))
                .thenReturn(RecipeDetailResponseDTO.builder().id(RECIPE_ID).build());

        recipeWriteService.updateRecipe(RECIPE_ID, request);

        verify(recipeMapper).updateById(recipeCaptor.capture());
        Recipe updated = recipeCaptor.getValue();

        assertEquals("new name", updated.getName());
        assertEquals("originalCover", updated.getCover());
        assertEquals("originalDesc", updated.getDesc());
    }

    @Test
    @DisplayName("更新不存在的菜谱抛出BusinessException")
    void testUpdateRecipe_RecipeNotFound() {
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(null);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recipeWriteService.updateRecipe(RECIPE_ID, request));

        assertEquals(ResultCode.RESOURCE_NOT_EXIST, exception.getCode());
        assertEquals("菜谱不存在", exception.getMessage());
        verify(recipeMapper, never()).updateById(any(Recipe.class));
    }

    @Test
    @DisplayName("非作者更新菜谱抛出BusinessException")
    void testUpdateRecipe_WrongAuthor() {
        Recipe recipe = createExistingRecipe();
        recipe.setAuthorId(999L);
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recipeWriteService.updateRecipe(RECIPE_ID, request));

        assertEquals(ResultCode.PERMISSION_ERROR, exception.getCode());
        assertEquals("无权修改该菜谱", exception.getMessage());
        verify(recipeMapper, never()).updateById(any(Recipe.class));
    }

    @Test
    @DisplayName("更新菜谱时校验分类委托给RecipeValidator")
    void testUpdateRecipe_CategoryValidation() {
        Recipe existingRecipe = createExistingRecipe();
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(existingRecipe);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("test");
        request.setCategory("health");
        when(recipeDtoAssembler.convertToDetailDTO(any(Recipe.class)))
                .thenReturn(RecipeDetailResponseDTO.builder().id(RECIPE_ID).build());

        recipeWriteService.updateRecipe(RECIPE_ID, request);

        verify(recipeValidator).validateCategory("health");
    }

    @Test
    @DisplayName("更新菜谱时校验JSON字段委托给RecipeValidator")
    void testUpdateRecipe_JsonFieldValidation() {
        Recipe existingRecipe = createExistingRecipe();
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(existingRecipe);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("test");
        request.setIngredients("[{\"name\":\"鸡蛋\"}]");
        request.setSteps("[{\"order\":1}]");
        request.setTags("[\"家常\"]");
        when(recipeDtoAssembler.convertToDetailDTO(any(Recipe.class)))
                .thenReturn(RecipeDetailResponseDTO.builder().id(RECIPE_ID).build());

        recipeWriteService.updateRecipe(RECIPE_ID, request);

        verify(recipeValidator).validateJsonField("[{\"name\":\"鸡蛋\"}]", "ingredients");
        verify(recipeValidator).validateJsonField("[{\"order\":1}]", "steps");
        verify(recipeValidator).validateJsonField("[\"家常\"]", "tags");
    }

    @Test
    @DisplayName("更新菜谱时请求中空字段不调用setter，保留原值")
    void testUpdateRecipe_NullFieldsNotSet() {
        Recipe existingRecipe = createExistingRecipe();
        existingRecipe.setName("original");
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(existingRecipe);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("new name");

        when(recipeDtoAssembler.convertToDetailDTO(any(Recipe.class)))
                .thenReturn(RecipeDetailResponseDTO.builder().id(RECIPE_ID).build());

        recipeWriteService.updateRecipe(RECIPE_ID, request);

        verify(recipeMapper).updateById(recipeCaptor.capture());
        Recipe updated = recipeCaptor.getValue();

        assertEquals("new name", updated.getName());
        assertEquals(existingRecipe.getCover(), updated.getCover());
        assertEquals(existingRecipe.getDesc(), updated.getDesc());
        assertEquals(existingRecipe.getCategory(), updated.getCategory());
        assertEquals(existingRecipe.getTips(), updated.getTips());
        assertEquals(existingRecipe.getCookTime(), updated.getCookTime());
        assertEquals(existingRecipe.getDifficulty(), updated.getDifficulty());
        assertEquals(existingRecipe.getCalories(), updated.getCalories());
    }

    @Test
    @DisplayName("更新菜谱后返回RecipeDetailResponseDTO")
    void testUpdateRecipe_ReturnsDetailDto() {
        Recipe existingRecipe = createExistingRecipe();
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(existingRecipe);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("test");

        RecipeDetailResponseDTO expectedDto = RecipeDetailResponseDTO.builder().id(RECIPE_ID).name("test").build();
        when(recipeDtoAssembler.convertToDetailDTO(any(Recipe.class))).thenReturn(expectedDto);

        RecipeDetailResponseDTO result = recipeWriteService.updateRecipe(RECIPE_ID, request);

        assertSame(expectedDto, result);
        verify(recipeDtoAssembler).convertToDetailDTO(any(Recipe.class));
    }

    @Test
    @DisplayName("更新菜谱时设置updatedAt时间戳")
    void testUpdateRecipe_UpdatedAtSet() {
        Recipe existingRecipe = createExistingRecipe();
        existingRecipe.setUpdatedAt(LocalDateTime.now().minusDays(1));
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(existingRecipe);

        RecipeUpdateRequestDTO request = new RecipeUpdateRequestDTO();
        request.setName("test");
        when(recipeDtoAssembler.convertToDetailDTO(any(Recipe.class)))
                .thenReturn(RecipeDetailResponseDTO.builder().id(RECIPE_ID).build());

        recipeWriteService.updateRecipe(RECIPE_ID, request);

        verify(recipeMapper).updateById(recipeCaptor.capture());
        Recipe updated = recipeCaptor.getValue();
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    // ==================== deleteRecipe ====================

    @Test
    @DisplayName("删除菜谱成功，级联删除点赞/收藏/评论")
    void testDeleteRecipe_Success() {
        Recipe recipe = createExistingRecipe();
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        recipeWriteService.deleteRecipe(RECIPE_ID);

        verify(likeMapper).delete(any(LambdaQueryWrapper.class));
        verify(collectionMapper).delete(any(LambdaQueryWrapper.class));
        verify(commentMapper).delete(any(LambdaQueryWrapper.class));
        verify(recipeMapper).deleteById(RECIPE_ID);
    }

    @Test
    @DisplayName("删除不存在的菜谱抛出BusinessException")
    void testDeleteRecipe_RecipeNotFound() {
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> recipeWriteService.deleteRecipe(RECIPE_ID));

        assertEquals(ResultCode.RESOURCE_NOT_EXIST, exception.getCode());
        assertEquals("菜谱不存在", exception.getMessage());
        verify(likeMapper, never()).delete(any());
        verify(collectionMapper, never()).delete(any());
        verify(commentMapper, never()).delete(any());
        verify(recipeMapper, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("非作者删除菜谱抛出BusinessException")
    void testDeleteRecipe_WrongAuthor() {
        Recipe recipe = createExistingRecipe();
        recipe.setAuthorId(999L);
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> recipeWriteService.deleteRecipe(RECIPE_ID));

        assertEquals(ResultCode.PERMISSION_ERROR, exception.getCode());
        assertEquals("无权删除该菜谱", exception.getMessage());
        verify(recipeMapper, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("删除菜谱时点赞删除使用targetId和targetType=1")
    void testDeleteRecipe_LikeDeleteWithTargetType() {
        Recipe recipe = createExistingRecipe();
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        recipeWriteService.deleteRecipe(RECIPE_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<Like>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(likeMapper).delete(wrapperCaptor.capture());
        assertNotNull(wrapperCaptor.getValue());
    }

    @Test
    @DisplayName("删除菜谱时收藏删除使用recipeId")
    void testDeleteRecipe_CollectionDeleteWithRecipeId() {
        Recipe recipe = createExistingRecipe();
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        recipeWriteService.deleteRecipe(RECIPE_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<Collection>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(collectionMapper).delete(wrapperCaptor.capture());
        assertNotNull(wrapperCaptor.getValue());
    }

    @Test
    @DisplayName("删除菜谱时评论删除使用recipeId")
    void testDeleteRecipe_CommentDeleteWithRecipeId() {
        Recipe recipe = createExistingRecipe();
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        recipeWriteService.deleteRecipe(RECIPE_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<Comment>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(commentMapper).delete(wrapperCaptor.capture());
        assertNotNull(wrapperCaptor.getValue());
    }

    @Test
    @DisplayName("删除菜谱时级联操作为like>collection>comment>recipe顺序")
    void testDeleteRecipe_CascadeOrder() {
        Recipe recipe = createExistingRecipe();
        when(recipeMapper.selectById(RECIPE_ID)).thenReturn(recipe);

        recipeWriteService.deleteRecipe(RECIPE_ID);

        InOrder inOrder = inOrder(likeMapper, collectionMapper, commentMapper, recipeMapper);
        inOrder.verify(likeMapper).delete(any(LambdaQueryWrapper.class));
        inOrder.verify(collectionMapper).delete(any(LambdaQueryWrapper.class));
        inOrder.verify(commentMapper).delete(any(LambdaQueryWrapper.class));
        inOrder.verify(recipeMapper).deleteById(RECIPE_ID);
    }

    // ==================== Helper Methods ====================

    private CreateRecipeRequestDTO createFullRequest() {
        CreateRecipeRequestDTO dto = new CreateRecipeRequestDTO();
        dto.setName("测试菜谱");
        dto.setCoverUrl("http://example.com/cover.jpg");
        dto.setDesc("这是一道测试菜谱");
        dto.setCategory("home");
        dto.setTips("小心烫手");
        dto.setCookTime("30分钟");
        dto.setDifficulty(2);
        dto.setCalories(500);
        dto.setImageUrls(List.of("http://example.com/img1.jpg"));
        dto.setIngredients("[{\"name\":\"鸡蛋\",\"quantity\":2,\"unit\":\"个\"}]");
        dto.setSteps("[{\"order\":1,\"description\":\"打鸡蛋\"}]");
        dto.setTags("[\"家常\"]");
        return dto;
    }

    private Recipe createExistingRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(RECIPE_ID);
        recipe.setName("original");
        recipe.setCover("originalCover");
        recipe.setImages(new ArrayList<>());
        recipe.setAuthorId(USER_ID);
        recipe.setDesc("originalDesc");
        recipe.setTips("originalTips");
        recipe.setCookTime("60分钟");
        recipe.setDifficulty(1);
        recipe.setCalories(300);
        recipe.setCategory("fast");
        recipe.setIngredients("[]");
        recipe.setSteps("[]");
        recipe.setTags("[]");
        recipe.setSource(2);
        recipe.setLikeCount(10);
        recipe.setCollectionCount(5);
        recipe.setCommentCount(3);
        recipe.setViewCount(100);
        recipe.setCreatedAt(LocalDateTime.now().minusDays(1));
        recipe.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return recipe;
    }

    private RecipeUpdateRequestDTO createFullUpdateRequest() {
        RecipeUpdateRequestDTO dto = new RecipeUpdateRequestDTO();
        dto.setName("new name");
        dto.setCoverUrl("http://example.com/new-cover.jpg");
        dto.setImageUrls(List.of("http://example.com/new-img1.jpg"));
        dto.setDesc("new desc");
        dto.setCategory("health");
        dto.setTips("new tips");
        dto.setCookTime("45分钟");
        dto.setDifficulty(3);
        dto.setCalories(400);
        dto.setIngredients("[{\"name\":\"新食材\"}]");
        dto.setSteps("[{\"order\":1,\"description\":\"新步骤\"}]");
        dto.setTags("[\"新标签\"]");
        return dto;
    }
}
