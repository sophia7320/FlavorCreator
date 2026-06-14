package flcr.backend.recipe.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;

public interface RecipeQueryService {
    Page<RecipeListItemResponseDTO> getRecipeList(RecipeListRequestDTO request);
    RecipeDetailResponseDTO getRecipeDetail(Long recipeId);
}
