package flcr.backend.recipe.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.aop.Public;
import flcr.backend.common.response.Response;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeGenerateRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeGenerateResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeRecommendResponseDTO;
import flcr.backend.recipe.service.RecipeGenerateService;
import flcr.backend.recipe.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeGenerateService recipeGenerateService;

    @PostMapping("/generate")
    public Response<RecipeGenerateResponseDTO> generateRecipe(@Valid @RequestBody RecipeGenerateRequestDTO request) {
        RecipeGenerateResponseDTO result = recipeGenerateService.generateRecipe(request);
        return Response.success(result);
    }

    @PostMapping
    public Response<Long> publishRecipe(@Valid @RequestBody PublishRecipeRequestDTO request) {
        Long recipeId = recipeService.publishRecipe(request);
        return Response.success(recipeId);
    }

    @Public
    @GetMapping("/list")
    public Response<Page<RecipeListItemResponseDTO>> getRecipeList(@Valid RecipeListRequestDTO request) {
        Page<RecipeListItemResponseDTO> result = recipeService.getRecipeList(request);
        return Response.success(result);
    }

    @Public
    @GetMapping("/{id}")
    public Response<RecipeDetailResponseDTO> getRecipeDetail(@PathVariable Long id) {
        RecipeDetailResponseDTO detail = recipeService.getRecipeDetail(id);
        return Response.success(detail);
    }

    @PostMapping("/apply")
    public Response<ApplyRecipeResponseDTO> apply(@Valid @RequestBody ApplyRecipeRequestDTO request) {
        ApplyRecipeResponseDTO result = recipeService.apply(request);
        return Response.success(result);
    }

    @GetMapping("/recommend")
    public Response<RecipeRecommendResponseDTO> recommend() {
        return Response.success(recipeService.recommend());
    }
}