package flcr.backend.user.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ImageScene;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.context.UserContext;
import flcr.backend.common.exception.BusinessException;
import flcr.backend.common.service.ImageUploadService;
import flcr.backend.recipe.entity.Recipe;
import flcr.backend.recipe.mapper.RecipeMapper;
import flcr.backend.user.DTO.request.UpdateUserInfoRequestDTO;
import flcr.backend.user.DTO.response.UserInfoResponseDTO;
import flcr.backend.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

    private final UserMapper userMapper;
    private final RecipeMapper recipeMapper;
    private final ObjectMapper objectMapper;
    private final ImageUploadService imageUploadService;

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
        if (request.getAvatar() != null){
            user.setAvatar(request.getAvatar());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
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

    @Transactional
    @Override
    public String uploadAvatar(MultipartFile file) {
        Long userId = UserContext.getUserId();
        String avatarUrl = imageUploadService.upload(file, ImageScene.AVATAR);

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setAvatar(avatarUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }

        return avatarUrl;
    }

    @Transactional
    @Override
    public String uploadBackground(MultipartFile file) {
        Long userId = UserContext.getUserId();
        String backgroundUrl = imageUploadService.upload(file, ImageScene.BACKGROUND);

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setBackground(backgroundUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }

        return backgroundUrl;
    }

    private UserInfoResponseDTO buildResponse(User user, Long userId) {

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
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .background(user.getBackground())
                .signature(user.getSignature())
                .gender(user.getGender())
                .address(user.getAddress())
                .age(user.getAge())
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
