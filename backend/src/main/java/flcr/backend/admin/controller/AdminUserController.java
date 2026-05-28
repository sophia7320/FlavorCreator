package flcr.backend.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.admin.DTO.request.AdminUserListRequestDTO;
import flcr.backend.admin.DTO.response.AdminUserResponseDTO;
import flcr.backend.admin.service.AdminUserService;
import flcr.backend.common.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Response<Page<AdminUserResponseDTO>> list(AdminUserListRequestDTO request) {
        Page<AdminUserResponseDTO> result = adminUserService.listUsers(request);
        return Response.success(result);
    }

    @GetMapping("/{id}")
    public Response<AdminUserResponseDTO> detail(@PathVariable Long id) {
        AdminUserResponseDTO result = adminUserService.getUserDetail(id);
        return Response.success(result);
    }

}
