package flcr.backend.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.community.entity.Collection;
import flcr.backend.community.entity.Like;
import flcr.backend.community.mapper.CollectionMapper;
import flcr.backend.community.mapper.LikeMapper;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.user.DTO.request.UpdateUserInfoRequestDTO;
import flcr.backend.user.DTO.response.UserInfoResponseDTO;
import flcr.backend.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

    private final UserMapper userMapper;
    private final RecipeMapper recipeMapper;
    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public UserInfoResponseDTO getInfo() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        return buildResponse(user, userId);
    }

    @Override
    @Transactional
    public UserInfoResponseDTO updateInfo(UpdateUserInfoRequestDTO request) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }
        if (request.getBackground() != null) {
            user.setBackground(request.getBackground());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getPreferences() != null) {
            try {
                user.setPreferences(objectMapper.writeValueAsString(request.getPreferences()));
            } catch (JsonProcessingException e) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "偏好设置序列化失败");
            }
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return buildResponse(user, userId);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        Long userId = UserContext.getUserId();
        // TODO: 对接 OSS，当前使用占位符
        String avatarUrl = "/uploads/avatar.jpg";

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setAvatar(avatarUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }

        return avatarUrl;
    }

    @Override
    public String uploadBackground(MultipartFile file) {
        Long userId = UserContext.getUserId();
        // TODO: 对接 OSS，当前使用占位符
        String backgroundUrl = "/uploads/background.jpg";

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setBackground(backgroundUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }

        return backgroundUrl;
    }

    private UserInfoResponseDTO buildResponse(User user, Long userId) {
        // 手机号脱敏
        String phone = null;
        if (user.getPhoneNumber() != null && user.getPhoneNumber().length() == 11) {
            phone = user.getPhoneNumber().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }
        if (user.getPhoneNumber() != null && user.getPhoneNumber().length() != 11) {
            phone = user.getPhoneNumber();
        }

        // 解析偏好设置
        UserInfoResponseDTO.PreferencesInfo preferences = null;
        if (user.getPreferences() != null) {
            try {
                UpdateUserInfoRequestDTO.Preferences prefs =
                        objectMapper.readValue(user.getPreferences(), UpdateUserInfoRequestDTO.Preferences.class);
                preferences = UserInfoResponseDTO.PreferencesInfo.builder()
                        .taste(prefs.getTaste())
                        .dietary(prefs.getDietary())
                        .cookTime(prefs.getCookTime())
                        .difficulty(prefs.getDifficulty())
                        .build();
            } catch (JsonProcessingException e) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "偏好设置解析失败");
            }
        }

        // 统计：用户所有菜谱的总点赞和收藏
        int likeCount = 0;
        int collectionCount = 0;

        LambdaQueryWrapper<Recipe> recipeWrapper = new LambdaQueryWrapper<>();
        recipeWrapper.eq(Recipe::getAuthorId, userId);
        List<Recipe> recipes = recipeMapper.selectList(recipeWrapper);

        for (Recipe recipe : recipes) {
            likeCount += recipe.getLikeCount();
            collectionCount += recipe.getCollectionCount();
        }

        return UserInfoResponseDTO.builder()
                .id((long) user.getId())
                .openid(user.getOpenid())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .background(user.getBackground())
                .signature(user.getSignature())
                .gender(user.getGender())
                .phone(phone)
                .preferences(preferences)
                .stats(UserInfoResponseDTO.StatsInfo.builder()
                        .followingCount(0)
                        .followerCount(0)
                        .likeCount(likeCount)
                        .collectionCount(collectionCount)
                        .build())
                .build();
    }
}
