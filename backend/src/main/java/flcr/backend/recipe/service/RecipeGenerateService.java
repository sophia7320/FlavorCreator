package flcr.backend.recipe.service;

import flcr.backend.recipe.DTO.request.RecipeGenerateRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeGenerateResponseDTO;

public interface RecipeGenerateService {

    RecipeGenerateResponseDTO generateRecipe(RecipeGenerateRequestDTO request);
}
