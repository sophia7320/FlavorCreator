package flcr.backend.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.context.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.user.DTO.response.MyCollectionResponseDTO;
import flcr.backend.user.DTO.response.MyLikeResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCenterServiceImplTest {

    @Mock private CollectionMapper collectionMapper;
    @Mock private LikeMapper likeMapper;
    @Mock private RecipeMapper recipeMapper;
    @Mock private UserMapper userMapper;
    @Mock private ObjectMapper objectMapper;
    @InjectMocks private UserCenterServiceImpl userCenterService;

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
    @DisplayName("getMyCollections返回收藏列表")
    void testGetMyCollections_Success() {
        Collection coll = new Collection();
        coll.setId(1L);
        coll.setUserId(USER_ID);
        coll.setRecipeId(10L);
        coll.setCreatedAt(LocalDateTime.now());

        Page<Collection> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(List.of(coll));

        when(collectionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        Recipe recipe = new Recipe();
        recipe.setId(10L);
        recipe.setName("收藏的菜谱");
        recipe.setCover("/cover.jpg");
        recipe.setAuthorId(200L);
        when(recipeMapper.selectBatchIds(any())).thenReturn(List.of(recipe));

        User author = new User();
        author.setId(200L);
        author.setNickname("作者");
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(author));

        Page<MyCollectionResponseDTO> result = userCenterService.getMyCollections(1, 20);
        assertEquals(1, result.getRecords().size());
        assertEquals("收藏的菜谱", result.getRecords().get(0).getRecipeName());
        assertEquals("作者", result.getRecords().get(0).getAuthorName());
    }

    @Test
    @DisplayName("getMyLikes返回点赞列表")
    void testGetMyLikes_Success() {
        Like like = new Like();
        like.setId(1L);
        like.setUserId(USER_ID);
        like.setTargetId(10L);
        like.setTargetType(1);
        like.setCreatedAt(LocalDateTime.now());

        Page<Like> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(List.of(like));

        when(likeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        Recipe recipe = new Recipe();
        recipe.setId(10L);
        recipe.setName("点赞的菜谱");
        recipe.setCover("/cover.jpg");
        recipe.setAuthorId(200L);
        when(recipeMapper.selectBatchIds(any())).thenReturn(List.of(recipe));

        User author = new User();
        author.setId(200L);
        author.setNickname("作者");
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(author));

        Page<MyLikeResponseDTO> result = userCenterService.getMyLikes(1, 20);
        assertEquals(1, result.getRecords().size());
        assertEquals("点赞的菜谱", result.getRecords().get(0).getRecipeName());
    }

    @Test
    @DisplayName("getMyRecipes返回发布列表")
    void testGetMyRecipes_Success() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("我的菜谱");
        recipe.setCover("/cover.jpg");
        recipe.setAuthorId(USER_ID);
        recipe.setCookTime("简单");
        recipe.setDifficulty(1);
        recipe.setLikeCount(10);
        recipe.setCollectionCount(5);
        recipe.setCommentCount(3);
        recipe.setViewCount(100);
        recipe.setCreatedAt(LocalDateTime.now());

        Page<Recipe> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(List.of(recipe));

        when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser(USER_ID));

        Page<RecipeListItemResponseDTO> result = userCenterService.getMyRecipes(1, 20);
        assertEquals(1, result.getRecords().size());
        assertEquals("我的菜谱", result.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("getMyRecipes正确解析JSON格式tags并转换难度")
    void testGetMyRecipes_ParseTagsAndDifficulty() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("测试菜谱");
        recipe.setAuthorId(USER_ID);
        recipe.setTags("[\"快手\",\"下饭\"]");
        recipe.setDifficulty(2);
        recipe.setCreatedAt(LocalDateTime.now());

        Page<Recipe> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(List.of(recipe));

        when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser(USER_ID));
        try {
            when(objectMapper.readValue("[\"快手\",\"下饭\"]", String[].class))
                    .thenReturn(new String[]{"快手", "下饭"});
        } catch (Exception ignored) {}
        Page<RecipeListItemResponseDTO> result = userCenterService.getMyRecipes(1, 20);
        assertEquals(1, result.getRecords().size());
        RecipeListItemResponseDTO dto = result.getRecords().get(0);
        assertEquals(2, dto.getTags().length);
        assertEquals("快手", dto.getTags()[0]);
        assertEquals("中等", dto.getDifficulty());
    }

    @Test
    @DisplayName("getMyRecipes tags为null返回空数组")
    void testGetMyRecipes_NullTags() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("无标签菜谱");
        recipe.setAuthorId(USER_ID);
        recipe.setCreatedAt(LocalDateTime.now());

        Page<Recipe> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(List.of(recipe));

        when(recipeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser(USER_ID));

        Page<RecipeListItemResponseDTO> result = userCenterService.getMyRecipes(1, 20);

        assertEquals(0, result.getRecords().get(0).getTags().length);
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setNickname("测试用户");
        user.setAvatar("/avatar.jpg");
        return user;
    }
}
