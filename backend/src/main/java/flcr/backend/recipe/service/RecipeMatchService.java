package flcr.backend.recipe.service;

import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;

public interface RecipeMatchService {
    ApplyRecipeResponseDTO apply(ApplyRecipeRequestDTO request);
}
