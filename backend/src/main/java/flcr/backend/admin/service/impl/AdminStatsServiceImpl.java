package flcr.backend.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.admin.DTO.response.AdminStatsResponseDTO;
import flcr.backend.admin.service.AdminStatsService;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.community.entity.Comment;
import flcr.backend.community.mapper.CommentMapper;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    private final UserMapper userMapper;
    private final RecipeMapper recipeMapper;
    private final CommentMapper commentMapper;

    @Override
    public AdminStatsResponseDTO getOverview() {
        long totalUsers = userMapper.selectCount(null);
        long totalRecipes = recipeMapper.selectCount(null);
        long totalComments = commentMapper.selectCount(null);

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LambdaQueryWrapper<User> newUserWrapper = new LambdaQueryWrapper<>();
        newUserWrapper.ge(User::getCreatedAt, todayStart);
        long todayNewUsers = userMapper.selectCount(newUserWrapper);

        AdminStatsResponseDTO.UserStats userStats = AdminStatsResponseDTO.UserStats.builder()
                .totalUsers(totalUsers)
                .todayNewUsers(todayNewUsers)
                .activeUsers(totalUsers)
                .build();

        AdminStatsResponseDTO.ContentStats contentStats = AdminStatsResponseDTO.ContentStats.builder()
                .totalRecipes(totalRecipes)
                .totalComments(totalComments)
                .build();

        AdminStatsResponseDTO.AuditStats auditStats = AdminStatsResponseDTO.AuditStats.builder()
                .pendingAudit(0)
                .approvedAudit(totalRecipes)
                .build();

        return AdminStatsResponseDTO.builder()
                .userStats(userStats)
                .contentStats(contentStats)
                .auditStats(auditStats)
                .build();
    }
}
