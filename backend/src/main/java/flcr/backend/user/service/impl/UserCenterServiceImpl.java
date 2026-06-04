package flcr.backend.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.TargetTypeConstants;
import flcr.backend.common.context.UserContext;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.user.DTO.response.MyCollectionResponseDTO;
import flcr.backend.user.DTO.response.MyLikeResponseDTO;
import flcr.backend.user.service.UserCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCenterServiceImpl implements UserCenterService {

    private final CollectionMapper collectionMapper;
    private final LikeMapper likeMapper;
    private final RecipeMapper recipeMapper;
    private final UserMapper userMapper;

    @Override
    public Page<MyCollectionResponseDTO> getMyCollections(Integer page, Integer size) {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId)
                .orderByDesc(Collection::getCreatedAt);

        Page<Collection> collectionPage = new Page<>(page, size);
        Page<Collection> result = collectionMapper.selectPage(collectionPage, wrapper);

        if (result.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        Set<Long> recipeIds = result.getRecords().stream()
                .map(Collection::getRecipeId)
                .collect(Collectors.toSet());

        Map<Long, Recipe> recipeMap;
        if (!recipeIds.isEmpty()) {
            List<Recipe> recipes = recipeMapper.selectBatchIds(recipeIds);
            recipeMap = recipes.stream().collect(Collectors.toMap(Recipe::getId, r -> r, (a, b) -> a));
        } else {
            recipeMap = Collections.emptyMap();
        }

        Set<Long> authorIds = recipeMap.values().stream()
                .map(Recipe::getAuthorId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap;
        if (!authorIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(authorIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        } else {
            userMap = Collections.emptyMap();
        }

        List<MyCollectionResponseDTO> dtos = result.getRecords().stream()
                .map(coll -> {
                    Recipe recipe = recipeMap.get(coll.getRecipeId());
                    User author = recipe != null ? userMap.get(recipe.getAuthorId()) : null;
                    return MyCollectionResponseDTO.builder()
                            .id(coll.getId())
                            .recipeId(coll.getRecipeId())
                            .recipeName(recipe != null ? recipe.getName() : null)
                            .cover(recipe != null ? recipe.getCover() : null)
                            .authorId(recipe != null ? recipe.getAuthorId() : null)
                            .authorName(author != null ? author.getNickname() : null)
                            .collectedAt(coll.getCreatedAt() != null ? coll.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                            .build();
                })
                .collect(Collectors.toList());

        Page<MyCollectionResponseDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    @Override
    public Page<MyLikeResponseDTO> getMyLikes(Integer page, Integer size) {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, TargetTypeConstants.RECIPE)
                .orderByDesc(Like::getCreatedAt);

        Page<Like> likePage = new Page<>(page, size);
        Page<Like> result = likeMapper.selectPage(likePage, wrapper);

        if (result.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        Set<Long> recipeIds = result.getRecords().stream()
                .map(Like::getTargetId)
                .collect(Collectors.toSet());

        Map<Long, Recipe> recipeMap;
        if (!recipeIds.isEmpty()) {
            List<Recipe> recipes = recipeMapper.selectBatchIds(recipeIds);
            recipeMap = recipes.stream().collect(Collectors.toMap(Recipe::getId, r -> r, (a, b) -> a));
        } else {
            recipeMap = Collections.emptyMap();
        }

        Set<Long> authorIds = recipeMap.values().stream()
                .map(Recipe::getAuthorId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap;
        if (!authorIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(authorIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        } else {
            userMap = Collections.emptyMap();
        }

        List<MyLikeResponseDTO> dtos = result.getRecords().stream()
                .map(like -> {
                    Recipe recipe = recipeMap.get(like.getTargetId());
                    User author = recipe != null ? userMap.get(recipe.getAuthorId()) : null;
                    return MyLikeResponseDTO.builder()
                            .id(like.getId())
                            .recipeId(like.getTargetId())
                            .recipeName(recipe != null ? recipe.getName() : null)
                            .cover(recipe != null ? recipe.getCover() : null)
                            .authorId(recipe != null ? recipe.getAuthorId() : null)
                            .authorName(author != null ? author.getNickname() : null)
                            .likedAt(like.getCreatedAt() != null ? like.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                            .build();
                })
                .collect(Collectors.toList());

        Page<MyLikeResponseDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    @Override
    public Page<RecipeListItemResponseDTO> getMyRecipes(Integer page, Integer size) {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipe::getAuthorId, userId)
                .orderByDesc(Recipe::getCreatedAt);

        Page<Recipe> recipePage = new Page<>(page, size);
        Page<Recipe> result = recipeMapper.selectPage(recipePage, wrapper);

        if (result.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }

        User user = userMapper.selectById(userId);

        List<RecipeListItemResponseDTO> dtos = result.getRecords().stream()
                .map(recipe -> RecipeListItemResponseDTO.builder()
                        .id(recipe.getId())
                        .name(recipe.getName())
                        .cover(recipe.getCover())
                        .author(RecipeListItemResponseDTO.AuthorInfo.builder()
                                .id(userId)
                                .nickname(user != null ? user.getNickname() : null)
                                .avatar(user != null ? user.getAvatar() : null)
                                .build())
                        .cookTime(recipe.getCookTime())
                        .difficulty(recipe.getDifficulty() != null ? String.valueOf(recipe.getDifficulty()) : null)
                        .calories(recipe.getCalories())
                        .tags(recipe.getTags() != null ? recipe.getTags().split(",") : new String[0])
                        .stats(RecipeListItemResponseDTO.RecipeStats.builder()
                                .likes(recipe.getLikeCount())
                                .collections(recipe.getCollectionCount())
                                .comments(recipe.getCommentCount())
                                .views(recipe.getViewCount())
                                .build())
                        .createdAt(recipe.getCreatedAt() != null ? recipe.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                        .build())
                .collect(Collectors.toList());

        Page<RecipeListItemResponseDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }
}
