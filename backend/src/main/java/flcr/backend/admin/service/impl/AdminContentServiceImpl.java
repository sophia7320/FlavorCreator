package flcr.backend.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.admin.DTO.request.*;
import flcr.backend.admin.DTO.response.AdminCommentResponseDTO;
import flcr.backend.admin.DTO.response.AdminRecipeResponseDTO;
import flcr.backend.admin.service.AdminContentService;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.constants.SourceConstants;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.community.entity.Comment;
import flcr.backend.community.mapper.CommentMapper;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private final RecipeMapper recipeMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public Page<AdminRecipeResponseDTO> listRecipes(AdminRecipeListRequestDTO request) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Recipe::getCreatedAt);

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Recipe::getName, request.getKeyword());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(Recipe::getStatus, request.getStatus());
        }
        if (request.getSource() != null) {
            wrapper.eq(Recipe::getSource, request.getSource());
        }

        Page<Recipe> recipePage = new Page<>(request.getPage(), request.getSize());
        Page<Recipe> result = recipeMapper.selectPage(recipePage, wrapper);

        if (result.getRecords().isEmpty()) {
            return new Page<>(request.getPage(), request.getSize(), 0);
        }

        Set<Long> authorIds = result.getRecords().stream()
                .map(Recipe::getAuthorId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap;
        if (!authorIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(authorIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        } else {
            userMap = Collections.emptyMap();
        }

        List<AdminRecipeResponseDTO> dtos = result.getRecords().stream()
                .map(recipe -> buildRecipeDTO(recipe, userMap.get(recipe.getAuthorId())))
                .collect(Collectors.toList());

        Page<AdminRecipeResponseDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    @Override
    public AdminRecipeResponseDTO getRecipeDetail(Long id) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }
        User user = userMapper.selectById(recipe.getAuthorId());
        return buildRecipeDTO(recipe, user);
    }

    @Override
    @Transactional
    public AdminRecipeResponseDTO createRecipe(AdminRecipeCreateRequestDTO request) {
        Recipe recipe = new Recipe();
        recipe.setName(request.getName());
        recipe.setCover(request.getCover());
        recipe.setImages(request.getImages());
        recipe.setIngredients(request.getIngredients());
        recipe.setSteps(request.getSteps());
        recipe.setTips(request.getTips());
        recipe.setCookTime(request.getCookTime());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setCalories(request.getCalories());
        recipe.setTags(request.getTags());
        recipe.setCategory(request.getCategory());
        recipe.setSource(request.getSource() != null ? request.getSource() : SourceConstants.SYSTEM);
        recipe.setLikeCount(0);
        recipe.setCollectionCount(0);
        recipe.setCommentCount(0);
        recipe.setViewCount(0);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        recipeMapper.insert(recipe);
        return buildRecipeDTO(recipe, null);
    }

    @Override
    @Transactional
    public AdminRecipeResponseDTO updateRecipe(Long id, AdminRecipeUpdateRequestDTO request) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }
        if (request.getName() != null) recipe.setName(request.getName());
        if (request.getCover() != null) recipe.setCover(request.getCover());
        if (request.getImages() != null) recipe.setImages(request.getImages());
        if (request.getIngredients() != null) recipe.setIngredients(request.getIngredients());
        if (request.getSteps() != null) recipe.setSteps(request.getSteps());
        if (request.getTips() != null) recipe.setTips(request.getTips());
        if (request.getCookTime() != null) recipe.setCookTime(request.getCookTime());
        if (request.getDifficulty() != null) recipe.setDifficulty(request.getDifficulty());
        if (request.getCalories() != null) recipe.setCalories(request.getCalories());
        if (request.getTags() != null) recipe.setTags(request.getTags());
        if (request.getCategory() != null) recipe.setCategory(request.getCategory());
        recipe.setUpdatedAt(LocalDateTime.now());
        recipeMapper.updateById(recipe);
        User user = userMapper.selectById(recipe.getAuthorId());
        return buildRecipeDTO(recipe, user);
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }
        recipeMapper.deleteById(id);
        log.info("管理员删除菜谱: id={}, name={}", id, recipe.getName());
    }

    @Override
    @Transactional
    public void updateRecipeStatus(Long id, AdminRecipeStatusRequestDTO request) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }
        recipe.setStatus(request.getStatus());
        recipe.setUpdatedAt(LocalDateTime.now());
        recipeMapper.updateById(recipe);
        log.info("管理员修改菜谱状态: id={}, status={}", id, request.getStatus());
    }

    @Override
    public Page<AdminCommentResponseDTO> listComments(AdminCommentListRequestDTO request) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreatedAt);

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Comment::getContent, request.getKeyword());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(Comment::getStatus, request.getStatus());
        }
        if (request.getRecipeId() != null) {
            wrapper.eq(Comment::getRecipeId, request.getRecipeId());
        }

        Page<Comment> commentPage = new Page<>(request.getPage(), request.getSize());
        Page<Comment> result = commentMapper.selectPage(commentPage, wrapper);

        if (result.getRecords().isEmpty()) {
            return new Page<>(request.getPage(), request.getSize(), 0);
        }

        Set<Long> userIds = result.getRecords().stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());
        Set<Long> recipeIds = result.getRecords().stream()
                .map(Comment::getRecipeId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = batchQueryUsers(userIds);
        Map<Long, Recipe> recipeMap = batchQueryRecipes(recipeIds);

        List<AdminCommentResponseDTO> dtos = result.getRecords().stream()
                .map(comment -> buildCommentDTO(comment,
                        userMap.get(comment.getUserId()),
                        recipeMap.get(comment.getRecipeId())))
                .collect(Collectors.toList());

        Page<AdminCommentResponseDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    @Override
    public AdminCommentResponseDTO getCommentDetail(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "评论不存在");
        }
        User user = userMapper.selectById(comment.getUserId());
        Recipe recipe = recipeMapper.selectById(comment.getRecipeId());
        return buildCommentDTO(comment, user, recipe);
    }

    @Override
    @Transactional
    public AdminCommentResponseDTO createComment(AdminCommentCreateRequestDTO request) {
        Recipe recipe = recipeMapper.selectById(request.getRecipeId());
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }

        Comment comment = new Comment();
        comment.setUserId(0L);
        comment.setRecipeId(request.getRecipeId());
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        return buildCommentDTO(comment, null, recipe);
    }

    @Override
    @Transactional
    public AdminCommentResponseDTO updateComment(Long id, AdminCommentUpdateRequestDTO request) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "评论不存在");
        }
        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.updateById(comment);
        User user = userMapper.selectById(comment.getUserId());
        Recipe recipe = recipeMapper.selectById(comment.getRecipeId());
        return buildCommentDTO(comment, user, recipe);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "评论不存在");
        }
        commentMapper.deleteById(id);
        log.info("管理员删除评论: id={}", id);
    }

    @Override
    @Transactional
    public void updateCommentStatus(Long id, AdminCommentStatusRequestDTO request) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "评论不存在");
        }
        comment.setStatus(request.getStatus());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.updateById(comment);
        log.info("管理员修改评论状态: id={}, status={}", id, request.getStatus());
    }

    private AdminRecipeResponseDTO buildRecipeDTO(Recipe recipe, User user) {
        return AdminRecipeResponseDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .cover(recipe.getCover())
                .authorId(recipe.getAuthorId())
                .authorName(user != null ? user.getNickname() : null)
                .category(recipe.getCategory())
                .cookTime(recipe.getCookTime())
                .difficulty(recipe.getDifficulty())
                .calories(recipe.getCalories())
                .source(recipe.getSource())
                .likeCount(recipe.getLikeCount())
                .collectionCount(recipe.getCollectionCount())
                .commentCount(recipe.getCommentCount())
                .viewCount(recipe.getViewCount())
                .status(recipe.getStatus())
                .createdAt(recipe.getCreatedAt() != null ? recipe.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .updatedAt(recipe.getUpdatedAt() != null ? recipe.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();
    }

    private AdminCommentResponseDTO buildCommentDTO(Comment comment, User user, Recipe recipe) {
        return AdminCommentResponseDTO.builder()
                .id(comment.getId())
                .recipeId(comment.getRecipeId())
                .recipeName(recipe != null ? recipe.getName() : null)
                .userId(comment.getUserId())
                .userName(user != null ? user.getNickname() : "管理员")
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .likeCount(comment.getLikeCount())
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .updatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();
    }

    private Map<Long, User> batchQueryUsers(Set<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        List<User> users = userMapper.selectBatchIds(userIds.stream().collect(Collectors.toList()));
        return users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    private Map<Long, Recipe> batchQueryRecipes(Set<Long> recipeIds) {
        if (recipeIds.isEmpty()) return Collections.emptyMap();
        List<Recipe> recipes = recipeMapper.selectBatchIds(recipeIds.stream().collect(Collectors.toList()));
        return recipes.stream().collect(Collectors.toMap(Recipe::getId, r -> r, (a, b) -> a));
    }
}
