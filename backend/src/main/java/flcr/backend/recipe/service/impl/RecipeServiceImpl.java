package flcr.backend.recipe.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.CreateRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeUpdateRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeRecommendResponseDTO;
import flcr.backend.recipe.service.RecipeMatchService;
import flcr.backend.recipe.service.RecipeQueryService;
import flcr.backend.recipe.service.RecipeRecommendService;
import flcr.backend.recipe.service.RecipeService;
import flcr.backend.recipe.service.RecipeWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeWriteService recipeWriteService;
    private final RecipeQueryService recipeQueryService;
    private final RecipeMatchService recipeMatchService;
    private final RecipeRecommendService recipeRecommendService;

    @Override
    @Transactional
    public Long publishRecipe(CreateRecipeRequestDTO request) {
        return recipeWriteService.publishRecipe(request);
    }

    @Override
    @Transactional
    public RecipeDetailResponseDTO updateRecipe(Long recipeId, RecipeUpdateRequestDTO request) {
        return recipeWriteService.updateRecipe(recipeId, request);
    }

    @Override
    @Transactional
    public void deleteRecipe(Long recipeId) {
        recipeWriteService.deleteRecipe(recipeId);
    }

    @Override
    public Page<RecipeListItemResponseDTO> getRecipeList(RecipeListRequestDTO request) {
        return recipeQueryService.getRecipeList(request);
    }

    @Override
    public RecipeDetailResponseDTO getRecipeDetail(Long recipeId) {
        return recipeQueryService.getRecipeDetail(recipeId);
    }

    @Override
    public ApplyRecipeResponseDTO apply(ApplyRecipeRequestDTO request) {
        return recipeMatchService.apply(request);
    }

    @Override
    public RecipeRecommendResponseDTO recommend() {
        return recipeRecommendService.recommend();
    }
}
