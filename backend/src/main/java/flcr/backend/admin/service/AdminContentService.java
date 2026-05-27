package flcr.backend.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.admin.DTO.request.*;
import flcr.backend.admin.DTO.response.AdminCommentResponseDTO;
import flcr.backend.admin.DTO.response.AdminRecipeResponseDTO;

public interface AdminContentService {

    Page<AdminRecipeResponseDTO> listRecipes(AdminRecipeListRequestDTO request);

    AdminRecipeResponseDTO getRecipeDetail(Long id);

    AdminRecipeResponseDTO createRecipe(AdminRecipeCreateRequestDTO request);

    AdminRecipeResponseDTO updateRecipe(Long id, AdminRecipeUpdateRequestDTO request);

    void deleteRecipe(Long id);

    Page<AdminCommentResponseDTO> listComments(AdminCommentListRequestDTO request);

    AdminCommentResponseDTO getCommentDetail(Long id);

    AdminCommentResponseDTO createComment(AdminCommentCreateRequestDTO request);

    AdminCommentResponseDTO updateComment(Long id, AdminCommentUpdateRequestDTO request);

    void deleteComment(Long id);
}
