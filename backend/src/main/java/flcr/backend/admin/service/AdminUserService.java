package flcr.backend.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.admin.DTO.request.AdminUserListRequestDTO;
import flcr.backend.admin.DTO.request.AdminUserStatusRequestDTO;
import flcr.backend.admin.DTO.response.AdminUserResponseDTO;

public interface AdminUserService {

    Page<AdminUserResponseDTO> listUsers(AdminUserListRequestDTO request);

    AdminUserResponseDTO getUserDetail(Long id);

    void updateUserStatus(Long id, AdminUserStatusRequestDTO request);
}
