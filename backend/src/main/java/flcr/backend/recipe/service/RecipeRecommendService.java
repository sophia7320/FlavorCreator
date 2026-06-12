package flcr.backend.recipe.service;

import flcr.backend.recipe.DTO.response.RecipeRecommendResponseDTO;

public interface RecipeRecommendService {
    RecipeRecommendResponseDTO recommend();
}
