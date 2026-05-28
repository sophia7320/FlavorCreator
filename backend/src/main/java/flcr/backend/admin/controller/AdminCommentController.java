package flcr.backend.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.admin.DTO.request.*;
import flcr.backend.admin.DTO.response.AdminCommentResponseDTO;
import flcr.backend.admin.service.AdminContentService;
import flcr.backend.common.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminContentService adminContentService;

    @GetMapping
    public Response<Page<AdminCommentResponseDTO>> list(AdminCommentListRequestDTO request) {
        Page<AdminCommentResponseDTO> result = adminContentService.listComments(request);
        return Response.success(result);
    }

    @GetMapping("/{id}")
    public Response<AdminCommentResponseDTO> detail(@PathVariable Long id) {
        AdminCommentResponseDTO result = adminContentService.getCommentDetail(id);
        return Response.success(result);
    }

    @PostMapping
    public Response<AdminCommentResponseDTO> create(@Valid @RequestBody AdminCommentCreateRequestDTO request) {
        AdminCommentResponseDTO result = adminContentService.createComment(request);
        return Response.success("创建成功", result);
    }

    @PutMapping("/{id}")
    public Response<AdminCommentResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody AdminCommentUpdateRequestDTO request) {
        AdminCommentResponseDTO result = adminContentService.updateComment(id, request);
        return Response.success("更新成功", result);
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        adminContentService.deleteComment(id);
        return Response.success("删除成功", null);
    }

}
