package flcr.backend.community;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Comment;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.CommentMapper;
import flcr.backend.community.mapper.LikeMapper;
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
class CommunityMapperTest {

    @Autowired private CommentMapper commentMapper;
    @Autowired private LikeMapper likeMapper;
    @Autowired private CollectionMapper collectionMapper;

    @Test
    @DisplayName("插入评论")
    void testInsertComment() {
        Comment comment = new Comment();
        comment.setUserId(1L);
        comment.setRecipeId(1L);
        comment.setContent("测试评论");
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        int result = commentMapper.insert(comment);
        assertEquals(1, result);
        assertNotNull(comment.getId());
    }

    @Test
    @DisplayName("按菜谱ID查询评论")
    void testSelectCommentByRecipeId() {
        Comment c1 = new Comment();
        c1.setUserId(1L); c1.setRecipeId(100L); c1.setContent("评论1");
        c1.setLikeCount(0); c1.setCreatedAt(LocalDateTime.now()); c1.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(c1);

        Comment c2 = new Comment();
        c2.setUserId(2L); c2.setRecipeId(100L); c2.setContent("评论2");
        c2.setLikeCount(0); c2.setCreatedAt(LocalDateTime.now()); c2.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(c2);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getRecipeId, 100L);
        List<Comment> comments = commentMapper.selectList(wrapper);

        assertEquals(2, comments.size());
    }

    @Test
    @DisplayName("插入点赞记录")
    void testInsertLike() {
        Like like = new Like();
        like.setUserId(1L);
        like.setTargetId(1L);
        like.setTargetType(1);
        like.setCreatedAt(LocalDateTime.now());

        assertEquals(1, likeMapper.insert(like));
    }

    @Test
    @DisplayName("查询点赞状态")
    void testSelectLikeCount() {
        Like like = new Like();
        like.setUserId(1L); like.setTargetId(1L); like.setTargetType(1);
        like.setCreatedAt(LocalDateTime.now());
        likeMapper.insert(like);

        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, 1L).eq(Like::getTargetId, 1L).eq(Like::getTargetType, 1);
        assertEquals(1, likeMapper.selectCount(wrapper));
    }

    @Test
    @DisplayName("插入收藏记录")
    void testInsertCollection() {
        Collection c = new Collection();
        c.setUserId(1L);
        c.setRecipeId(1L);
        c.setCreatedAt(LocalDateTime.now());

        assertEquals(1, collectionMapper.insert(c));
    }

    @Test
    @DisplayName("查询收藏状态")
    void testSelectCollectionCount() {
        Collection c = new Collection();
        c.setUserId(1L); c.setRecipeId(1L);
        c.setCreatedAt(LocalDateTime.now());
        collectionMapper.insert(c);

        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, 1L).eq(Collection::getRecipeId, 1L);
        assertEquals(1, collectionMapper.selectCount(wrapper));
    }
}
