package flcr.backend.recipe.service;

import flcr.backend.recipe.DTO.request.CreateRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeUpdateRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;

public interface RecipeWriteService {
    Long publishRecipe(CreateRecipeRequestDTO request);
    RecipeDetailResponseDTO updateRecipe(Long recipeId, RecipeUpdateRequestDTO request);
    void deleteRecipe(Long recipeId);
}
