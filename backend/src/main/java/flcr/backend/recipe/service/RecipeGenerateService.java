package flcr.backend.recipe.service;

import flcr.backend.recipe.DTO.request.Recipe_Request;
import flcr.backend.recipe.DTO.response.Recipe_Response;

public interface RecipeGenerateService {

    Recipe_Response generateRecipe(Recipe_Request request);
}
