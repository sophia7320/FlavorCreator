package flcr.backend.admin.service.impl;

import flcr.backend.admin.DTO.response.AdminStatsResponseDTO;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.community.mapper.CommentMapper;
import flcr.backend.recipe.mapper.RecipeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private RecipeMapper recipeMapper;
    @Mock private CommentMapper commentMapper;
    @InjectMocks private AdminStatsServiceImpl adminStatsService;

    @Test
    @DisplayName("getOverview返回正确的统计数据")
    void testGetOverview_Success() {
        when(userMapper.selectCount(any())).thenReturn(100L);
        when(recipeMapper.selectCount(any())).thenReturn(50L);
        when(commentMapper.selectCount(any())).thenReturn(200L);

        AdminStatsResponseDTO result = adminStatsService.getOverview();

        assertEquals(100, result.getUserStats().getTotalUsers());
        assertEquals(50, result.getContentStats().getTotalRecipes());
        assertEquals(200, result.getContentStats().getTotalComments());
        assertNotNull(result.getAuditStats());
    }
}
