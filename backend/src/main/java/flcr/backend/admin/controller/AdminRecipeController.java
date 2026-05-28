package flcr.backend.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.admin.DTO.request.*;
import flcr.backend.admin.DTO.response.AdminRecipeResponseDTO;
import flcr.backend.admin.service.AdminContentService;
import flcr.backend.common.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/recipes")
@RequiredArgsConstructor
public class AdminRecipeController {

    private final AdminContentService adminContentService;

    @GetMapping
    public Response<Page<AdminRecipeResponseDTO>> list(AdminRecipeListRequestDTO request) {
        Page<AdminRecipeResponseDTO> result = adminContentService.listRecipes(request);
        return Response.success(result);
    }

    @GetMapping("/{id}")
    public Response<AdminRecipeResponseDTO> detail(@PathVariable Long id) {
        AdminRecipeResponseDTO result = adminContentService.getRecipeDetail(id);
        return Response.success(result);
    }

    @PostMapping
    public Response<AdminRecipeResponseDTO> create(@Valid @RequestBody AdminRecipeCreateRequestDTO request) {
        AdminRecipeResponseDTO result = adminContentService.createRecipe(request);
        return Response.success("创建成功", result);
    }

    @PutMapping("/{id}")
    public Response<AdminRecipeResponseDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody AdminRecipeUpdateRequestDTO request) {
        AdminRecipeResponseDTO result = adminContentService.updateRecipe(id, request);
        return Response.success("更新成功", result);
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        adminContentService.deleteRecipe(id);
        return Response.success("删除成功", null);
    }

}
