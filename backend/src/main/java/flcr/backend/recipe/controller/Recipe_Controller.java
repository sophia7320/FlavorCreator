package flcr.backend.recipe.controller;

import flcr.backend.common.response.Response;
import flcr.backend.recipe.DTO.request.Recipe_Request;
import flcr.backend.recipe.DTO.response.Recipe_Response;
import flcr.backend.recipe.service.RecipeGenerateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipe/generate")
@RequiredArgsConstructor
public class Recipe_Controller {

    private final RecipeGenerateService recipeGenerateService;

    @PostMapping
    public Response<Recipe_Response> handleRecipeRequest(@Valid @RequestBody Recipe_Request request) {
        Recipe_Response responseData = recipeGenerateService.generateRecipe(request);
        return Response.success(responseData);
    }
}
