package flcr.backend.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.constants.TargetTypeConstants;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.community.DTO.request.CommentRequestDTO;
import flcr.backend.community.DTO.response.CommentResponseDTO;
import flcr.backend.community.DTO.response.LikeCollectResponseDTO;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public LikeCollectResponseDTO likeRecipe(Long recipeId) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetId, recipeId)
                .eq(Like::getTargetType, TargetTypeConstants.RECIPE);

        if (likeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已经点赞过");
        }

        Like like = new Like();
        like.setUserId(userId);
        like.setTargetId(recipeId);
        like.setTargetType(TargetTypeConstants.RECIPE);
        like.setCreatedAt(LocalDateTime.now());
        try {
            likeMapper.insert(like);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已经点赞过");
        }

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
                .eq(Like::getTargetType, TargetTypeConstants.RECIPE);
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
        try {
            collectionMapper.insert(collection);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已经收藏过");
        }

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

        // 1. Query parent comments (paged)
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getRecipeId, recipeId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreatedAt);

        Page<Comment> commentPage = new Page<>(page, size);
        Page<Comment> result = commentMapper.selectPage(commentPage, wrapper);
        List<Comment> comments = result.getRecords();

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Collect all comment IDs and user IDs
        List<Long> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());

        // 3. Batch query replies (parentId IN commentIds)
        LambdaQueryWrapper<Comment> replyQuery = new LambdaQueryWrapper<>();
        replyQuery.in(Comment::getParentId, commentIds)
                .orderByAsc(Comment::getCreatedAt);
        List<Comment> replies = commentMapper.selectList(replyQuery);

        // 4. Group replies by parentId and collect reply user IDs
        Map<Long, List<Comment>> repliesByParentId = replies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));
        userIds.addAll(replies.stream().map(Comment::getUserId).collect(Collectors.toSet()));

        // 5. Batch query all users (both comment authors and reply authors)
        Map<Long, User> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
            userMap = users.stream().collect(Collectors.toMap(
                    User::getId, u -> u, (a, b) -> a));
        }

        // 6. Batch check liked status for all comments
        Set<Long> likedCommentIds = Collections.emptySet();
        if (userId != null) {
            Set<Long> allCommentIds = new HashSet<>(commentIds);
            allCommentIds.addAll(replies.stream().map(Comment::getId).collect(Collectors.toSet()));
            if (!allCommentIds.isEmpty()) {
                LambdaQueryWrapper<Like> likeQuery = new LambdaQueryWrapper<>();
                likeQuery.eq(Like::getUserId, userId)
                        .eq(Like::getTargetType, TargetTypeConstants.COMMENT)
                        .in(Like::getTargetId, new ArrayList<>(allCommentIds));
                List<Like> likes = likeMapper.selectList(likeQuery);
                likedCommentIds = likes.stream().map(Like::getTargetId).collect(Collectors.toSet());
            }
        }

        // 7. Build DTOs from pre-fetched data (no additional DB queries)
        final Set<Long> finalLikedCommentIds = likedCommentIds;
        final Map<Long, User> finalUserMap = userMap;
        return comments.stream()
                .map(comment -> buildCommentDTO(comment, finalUserMap, repliesByParentId, finalLikedCommentIds))
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

        return buildSingleCommentDTO(comment, userId);
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
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetId, commentId)
                .eq(Like::getTargetType, TargetTypeConstants.COMMENT);

        if (likeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已经点赞过");
        }

        Like like = new Like();
        like.setUserId(userId);
        like.setTargetId(commentId);
        like.setTargetType(TargetTypeConstants.COMMENT);
        like.setCreatedAt(LocalDateTime.now());
        likeMapper.insert(like);

        LambdaUpdateWrapper<Comment> likeWrapper = new LambdaUpdateWrapper<>();
        likeWrapper.eq(Comment::getId, commentId)
                .setSql("like_count = like_count + 1");
        commentMapper.update(null, likeWrapper);
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetId, commentId)
                .eq(Like::getTargetType, TargetTypeConstants.COMMENT);
        likeMapper.delete(wrapper);

        LambdaUpdateWrapper<Comment> unlikeWrapper = new LambdaUpdateWrapper<>();
        unlikeWrapper.eq(Comment::getId, commentId)
                .setSql("like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END");
        commentMapper.update(null, unlikeWrapper);
    }

    // ==================== 私有辅助方法 ====================

    private CommentResponseDTO buildCommentDTO(Comment comment,
                                                Map<Long, User> userMap,
                                                Map<Long, List<Comment>> repliesByParentId,
                                                Set<Long> likedCommentIds) {
        User user = userMap.get(comment.getUserId());
        List<Comment> replies = repliesByParentId.getOrDefault(comment.getId(), Collections.emptyList());

        List<CommentResponseDTO.CommentReplyDTO> replyDTOs = replies.stream()
                .map(reply -> {
                    User replyUser = userMap.get(reply.getUserId());
                    return CommentResponseDTO.CommentReplyDTO.builder()
                            .id(reply.getId())
                            .user(CommentResponseDTO.UserInfo.builder()
                                    .id(replyUser != null ? replyUser.getId() : null)
                                    .nickname(replyUser != null ? replyUser.getNickname() : "未知用户")
                                    .avatar(replyUser != null ? replyUser.getAvatar() : "")
                                    .build())
                            .content(reply.getContent())
                            .createdAt(reply.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .build();
                })
                .collect(Collectors.toList());

        boolean isLiked = likedCommentIds.contains(comment.getId());

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .user(CommentResponseDTO.UserInfo.builder()
                        .id(user != null ? user.getId() : null)
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

    private CommentResponseDTO buildSingleCommentDTO(Comment comment, Long userId) {
        User user = userMapper.selectById(comment.getUserId());

        List<CommentResponseDTO.CommentReplyDTO> replyDTOs = Collections.emptyList();
        if (comment.getParentId() == null) {
            LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Comment::getParentId, comment.getId())
                    .orderByAsc(Comment::getCreatedAt);
            List<Comment> replies = commentMapper.selectList(replyWrapper);
            replyDTOs = replies.stream()
                    .map(reply -> {
                        User replyUser = userMapper.selectById(reply.getUserId());
                        return CommentResponseDTO.CommentReplyDTO.builder()
                                .id(reply.getId())
                                .user(CommentResponseDTO.UserInfo.builder()
                                        .id(replyUser != null ? replyUser.getId() : null)
                                        .nickname(replyUser != null ? replyUser.getNickname() : "未知用户")
                                        .avatar(replyUser != null ? replyUser.getAvatar() : "")
                                        .build())
                                .content(reply.getContent())
                                .createdAt(reply.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        boolean isLiked = userId != null && checkLiked(userId, comment.getId(), TargetTypeConstants.COMMENT);

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .user(CommentResponseDTO.UserInfo.builder()
                        .id(user != null ? user.getId() : null)
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
        if (recipe == null) {
            return LikeCollectResponseDTO.builder()
                    .isLiked(false)
                    .likeCount(0)
                    .isCollected(false)
                    .collectionCount(0)
                    .build();
        }

        boolean isLiked = checkLiked(userId, recipeId, TargetTypeConstants.RECIPE);
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
}
