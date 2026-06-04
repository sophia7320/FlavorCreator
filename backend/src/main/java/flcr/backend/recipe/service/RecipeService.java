package flcr.backend.recipe.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.recipe.DTO.request.ApplyRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.ApplyRecipeResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailResponseDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecipeService {

    Long publishRecipe(PublishRecipeRequestDTO request, MultipartFile cover, List<MultipartFile> images);

    Page<RecipeListItemResponseDTO> getRecipeList(RecipeListRequestDTO request);

    RecipeDetailResponseDTO getRecipeDetail(Long recipeId);

    ApplyRecipeResponseDTO apply(ApplyRecipeRequestDTO request);
}
