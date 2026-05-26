package flcr.backend.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.admin.DTO.request.AdminUserListRequestDTO;
import flcr.backend.admin.DTO.request.AdminUserStatusRequestDTO;
import flcr.backend.admin.DTO.response.AdminUserResponseDTO;
import flcr.backend.admin.service.AdminUserService;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;

    @Override
    public Page<AdminUserResponseDTO> listUsers(AdminUserListRequestDTO request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreatedAt);

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(User::getNickname, request.getKeyword())
                    .or().like(User::getPhoneNumber, request.getKeyword());
        }

        Page<User> userPage = new Page<>(request.getPage(), request.getSize());
        Page<User> result = userMapper.selectPage(userPage, wrapper);

        List<AdminUserResponseDTO> dtos = result.getRecords().stream()
                .map(this::buildUserDTO)
                .collect(Collectors.toList());

        Page<AdminUserResponseDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    @Override
    public AdminUserResponseDTO getUserDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "用户不存在");
        }
        return buildUserDTO(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, AdminUserStatusRequestDTO request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_EXIST, "用户不存在");
        }
        user.setStatus(request.getStatus());
        userMapper.updateById(user);
        log.info("管理员修改用户状态: id={}, status={}", id, request.getStatus());
    }

    private AdminUserResponseDTO buildUserDTO(User user) {
        return AdminUserResponseDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();
    }
}
