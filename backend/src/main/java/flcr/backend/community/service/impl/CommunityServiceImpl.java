package flcr.backend.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.FileStorageService;
import flcr.backend.community.DTO.request.CommentRequestDTO;
import flcr.backend.community.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.community.DTO.request.RecipeListRequestDTO;
import flcr.backend.community.DTO.response.CommentResponseDTO;
import flcr.backend.community.DTO.response.LikeCollectResponseDTO;
import flcr.backend.community.DTO.response.RecipeDetailDTO;
import flcr.backend.community.DTO.response.RecipeListItemDTO;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Comment;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.CommentMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.community.service.CommunityService;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final RecipeMapper recipeMapper;
    private final CommentMapper commentMapper;
    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public Long publishRecipe(PublishRecipeRequestDTO request, MultipartFile cover,
                              List<MultipartFile> images) {
        Long userId = UserContext.getUserId();
        String coverUrl = cover != null ? fileStorageService.store(cover, "recipe-cover") : "";
        List<String> imageUrls = new ArrayList<>();
        if (images != null) {
            for (MultipartFile image : images) {
                imageUrls.add(fileStorageService.store(image, "recipe-image"));
            }
        }

        Recipe recipe = new Recipe();
        recipe.setName(request.getName());
        recipe.setCover(coverUrl);
        try {
            recipe.setImages(objectMapper.writeValueAsString(imageUrls));
            recipe.setIngredients(request.getIngredients());
            recipe.setSteps(request.getSteps());
            recipe.setTags(request.getTags());
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON处理失败");
        }
        recipe.setAuthorId(userId);
        recipe.setCategory(request.getCategory());
        recipe.setTips(request.getTips());
        recipe.setCookTime(request.getCookTime());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setCalories(request.getCalories());
        recipe.setSource(2); // 用户发布
        recipe.setLikeCount(0);
        recipe.setCollectionCount(0);
        recipe.setCommentCount(0);
        recipe.setViewCount(0);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());

        recipeMapper.insert(recipe);
        return recipe.getId();
    }

    @Override
    public Page<RecipeListItemDTO> getRecipeList(RecipeListRequestDTO request) {
        Page<Recipe> recipePage = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();

        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            wrapper.eq(Recipe::getCategory, request.getCategory());
        }
        if (request.getDifficulty() != null && !request.getDifficulty().isEmpty()) {
            wrapper.eq(Recipe::getDifficulty, convertDifficulty(request.getDifficulty()));
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Recipe::getName, request.getKeyword());
        }

        wrapper.orderByDesc(Recipe::getCreatedAt);
        Page<Recipe> result = recipeMapper.selectPage(recipePage, wrapper);

        List<RecipeListItemDTO> dtoList = result.getRecords().stream()
                .map(this::convertToListItemDTO)
                .collect(Collectors.toList());

        Page<RecipeListItemDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public RecipeDetailDTO getRecipeDetail(Long recipeId) {
        Long userId = UserContext.getUserId();
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "菜谱不存在");
        }

        RecipeDetailDTO dto = convertToDetailDTO(recipe);

        LambdaUpdateWrapper<Recipe> viewWrapper = new LambdaUpdateWrapper<>();
        viewWrapper.eq(Recipe::getId, recipeId)
                .setSql("view_count = view_count + 1");
        recipeMapper.update(null, viewWrapper);

        if (userId != null) {
            dto.setIsLiked(checkLiked(userId, recipeId, 1));
            dto.setIsCollected(checkCollected(userId, recipeId));
        } else {
            dto.setIsLiked(false);
            dto.setIsCollected(false);
        }

        return dto;
    }

    @Override
    @Transactional
    public LikeCollectResponseDTO likeRecipe(Long recipeId) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetId, recipeId)
                .eq(Like::getTargetType, 1);

        if (likeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已经点赞过");
        }

        Like like = new Like();
        like.setUserId(userId);
        like.setTargetId(recipeId);
        like.setTargetType(1);
        like.setCreatedAt(LocalDateTime.now());
        likeMapper.insert(like);

        LambdaUpdateWrapper<Recipe> likeWrapper = new LambdaUpdateWrapper<>();
        likeWrapper.eq(Recipe::getId, recipeId)
                .setSql("like_count = like_count + 1");
        recipeMapper.update(null, likeWrapper);

        return buildLikeCollectResponse(recipeId, userId);
    }

    @Override
    @Transactional
    public LikeCollectResponseDTO unlikeRecipe(Long recipeId) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetId, recipeId)
                .eq(Like::getTargetType, 1);
        likeMapper.delete(wrapper);

        LambdaUpdateWrapper<Recipe> unlikeWrapper = new LambdaUpdateWrapper<>();
        unlikeWrapper.eq(Recipe::getId, recipeId)
                .setSql("like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END");
        recipeMapper.update(null, unlikeWrapper);

        return buildLikeCollectResponse(recipeId, userId);
    }

    @Override
    @Transactional
    public LikeCollectResponseDTO collectRecipe(Long recipeId) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId)
                .eq(Collection::getRecipeId, recipeId);

        if (collectionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已经收藏过");
        }

        Collection collection = new Collection();
        collection.setUserId(userId);
        collection.setRecipeId(recipeId);
        collection.setCreatedAt(LocalDateTime.now());
        collectionMapper.insert(collection);

        LambdaUpdateWrapper<Recipe> collectWrapper = new LambdaUpdateWrapper<>();
        collectWrapper.eq(Recipe::getId, recipeId)
                .setSql("collection_count = collection_count + 1");
        recipeMapper.update(null, collectWrapper);

        return buildLikeCollectResponse(recipeId, userId);
    }

    @Override
    @Transactional
    public LikeCollectResponseDTO uncollectRecipe(Long recipeId) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId)
                .eq(Collection::getRecipeId, recipeId);
        collectionMapper.delete(wrapper);

        LambdaUpdateWrapper<Recipe> uncollectWrapper = new LambdaUpdateWrapper<>();
        uncollectWrapper.eq(Recipe::getId, recipeId)
                .setSql("collection_count = CASE WHEN collection_count > 0 THEN collection_count - 1 ELSE 0 END");
        recipeMapper.update(null, uncollectWrapper);

        return buildLikeCollectResponse(recipeId, userId);
    }

    @Override
    public List<CommentResponseDTO> getComments(Long recipeId, Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getRecipeId, recipeId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreatedAt);

        Page<Comment> commentPage = new Page<>(page, size);
        Page<Comment> result = commentMapper.selectPage(commentPage, wrapper);

        return result.getRecords().stream()
                .map(comment -> convertToCommentDTO(comment, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDTO addComment(Long recipeId, CommentRequestDTO request) {
        Long userId = UserContext.getUserId();
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setRecipeId(recipeId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        commentMapper.insert(comment);

        LambdaUpdateWrapper<Recipe> commentWrapper = new LambdaUpdateWrapper<>();
        commentWrapper.eq(Recipe::getId, recipeId)
                .setSql("comment_count = comment_count + 1");
        recipeMapper.update(null, commentWrapper);

        return convertToCommentDTO(comment, userId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = UserContext.getUserId();
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_ERROR, "无权限删除该评论");
        }

        commentMapper.deleteById(commentId);

        LambdaUpdateWrapper<Recipe> delCommentWrapper = new LambdaUpdateWrapper<>();
        delCommentWrapper.eq(Recipe::getId, comment.getRecipeId())
                .setSql("comment_count = CASE WHEN comment_count > 0 THEN comment_count - 1 ELSE 0 END");
        recipeMapper.update(null, delCommentWrapper);
    }

    @Override
    @Transactional
    public void likeComment(Long commentId) {
        // TODO: 实现评论点赞逻辑
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId) {
        // TODO: 实现取消评论点赞逻辑
    }

    // ==================== 私有辅助方法 ====================

    private RecipeListItemDTO convertToListItemDTO(Recipe recipe) {
        User author = userMapper.selectById(recipe.getAuthorId());

        String[] tags = {};
        try {
            if (recipe.getTags() != null) {
                tags = objectMapper.readValue(recipe.getTags(), String[].class);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON解析失败");
        }

        return RecipeListItemDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .cover(recipe.getCover())
                .author(RecipeListItemDTO.AuthorInfo.builder()
                        .id(author != null ? (long) author.getId() : null)
                        .nickname(author != null ? author.getNickname() : "未知用户")
                        .avatar(author != null ? author.getAvatar() : "")
                        .build())
                .cookTime(recipe.getCookTime())
                .difficulty(convertDifficultyToString(recipe.getDifficulty()))
                .calories(recipe.getCalories())
                .tags(tags)
                .stats(RecipeListItemDTO.RecipeStats.builder()
                        .likes(recipe.getLikeCount())
                        .collections(recipe.getCollectionCount())
                        .comments(recipe.getCommentCount())
                        .views(recipe.getViewCount())
                        .build())
                .createdAt(recipe.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    private RecipeDetailDTO convertToDetailDTO(Recipe recipe) {
        User author = userMapper.selectById(recipe.getAuthorId());

        List<String> images = new ArrayList<>();
        try {
            if (recipe.getImages() != null) {
                images = objectMapper.readValue(recipe.getImages(), new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON解析失败");
        }

        List<RecipeDetailDTO.IngredientItem> ingredients = new ArrayList<>();
        List<RecipeDetailDTO.StepItem> steps = new ArrayList<>();
        String[] tags = {};

        try {
            if (recipe.getIngredients() != null) {
                ingredients = objectMapper.readValue(recipe.getIngredients(),
                    new TypeReference<List<RecipeDetailDTO.IngredientItem>>() {});
            }
            if (recipe.getSteps() != null) {
                steps = objectMapper.readValue(recipe.getSteps(),
                    new TypeReference<List<RecipeDetailDTO.StepItem>>() {});
            }
            if (recipe.getTags() != null) {
                tags = objectMapper.readValue(recipe.getTags(), String[].class);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "JSON解析失败");
        }

        return RecipeDetailDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .cover(recipe.getCover())
                .images(images)
                .author(RecipeDetailDTO.AuthorInfo.builder()
                        .id(author != null ? (long) author.getId() : null)
                        .nickname(author != null ? author.getNickname() : "未知用户")
                        .avatar(author != null ? author.getAvatar() : "")
                        .build())
                .ingredients(ingredients)
                .steps(steps)
                .tips(recipe.getTips())
                .cookTime(recipe.getCookTime())
                .difficulty(convertDifficultyToString(recipe.getDifficulty()))
                .calories(recipe.getCalories())
                .tags(tags)
                .stats(RecipeDetailDTO.RecipeStats.builder()
                        .likes(recipe.getLikeCount())
                        .collections(recipe.getCollectionCount())
                        .comments(recipe.getCommentCount())
                        .views(recipe.getViewCount())
                        .build())
                .build();
    }

    private CommentResponseDTO convertToCommentDTO(Comment comment, Long userId) {
        User user = userMapper.selectById(comment.getUserId());

        LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(Comment::getParentId, comment.getId())
                .orderByAsc(Comment::getCreatedAt);
        List<Comment> replies = commentMapper.selectList(replyWrapper);

        List<CommentResponseDTO.CommentReplyDTO> replyDTOs = replies.stream()
                .map(reply -> {
                    User replyUser = userMapper.selectById(reply.getUserId());
                    return CommentResponseDTO.CommentReplyDTO.builder()
                            .id(reply.getId())
                            .user(CommentResponseDTO.UserInfo.builder()
                                    .id(replyUser != null ? (long) replyUser.getId() : null)
                                    .nickname(replyUser != null ? replyUser.getNickname() : "未知用户")
                                    .avatar(replyUser != null ? replyUser.getAvatar() : "")
                                    .build())
                            .content(reply.getContent())
                            .createdAt(reply.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .build();
                })
                .collect(Collectors.toList());

        boolean isLiked = userId != null && checkLiked(userId, comment.getId(), 2);

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .user(CommentResponseDTO.UserInfo.builder()
                        .id(user != null ? (long) user.getId() : null)
                        .nickname(user != null ? user.getNickname() : "未知用户")
                        .avatar(user != null ? user.getAvatar() : "")
                        .build())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .isLiked(isLiked)
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .replies(replyDTOs)
                .build();
    }

    private LikeCollectResponseDTO buildLikeCollectResponse(Long recipeId, Long userId) {
        Recipe recipe = recipeMapper.selectById(recipeId);
        boolean isLiked = checkLiked(userId, recipeId, 1);
        boolean isCollected = checkCollected(userId, recipeId);

        return LikeCollectResponseDTO.builder()
                .isLiked(isLiked)
                .likeCount(recipe.getLikeCount())
                .isCollected(isCollected)
                .collectionCount(recipe.getCollectionCount())
                .build();
    }

    private boolean checkLiked(Long userId, Long targetId, Integer targetType) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetId, targetId)
                .eq(Like::getTargetType, targetType);
        return likeMapper.selectCount(wrapper) > 0;
    }

    private boolean checkCollected(Long userId, Long recipeId) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId)
                .eq(Collection::getRecipeId, recipeId);
        return collectionMapper.selectCount(wrapper) > 0;
    }

    private Integer convertDifficulty(String difficulty) {
        switch (difficulty) {
            case "simple": return 1;
            case "medium": return 2;
            case "hard": return 3;
            default: return null;
        }
    }

    private String convertDifficultyToString(Integer difficulty) {
        if (difficulty == null) return "";
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "中等";
            case 3: return "困难";
            default: return "未知";
        }
    }
}
