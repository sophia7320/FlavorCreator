package flcr.backend.recipe.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.CreateRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeUpdateRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeRecommendResponseDTO;

public interface RecipeService {

    Long publishRecipe(CreateRecipeRequestDTO request);

    RecipeDetailResponseDTO updateRecipe(Long recipeId, RecipeUpdateRequestDTO request);

    void deleteRecipe(Long recipeId);

    Page<RecipeListItemResponseDTO> getRecipeList(RecipeListRequestDTO request);

    RecipeDetailResponseDTO getRecipeDetail(Long recipeId);

    ApplyRecipeResponseDTO apply(ApplyRecipeRequestDTO request);

    RecipeRecommendResponseDTO recommend();
}
