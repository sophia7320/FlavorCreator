package flcr.backend.recipe.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.aop.Public;
import flcr.backend.common.response.Response;
import flcr.backend.recipe.DTO.request.PublishRecipeRequestDTO;
import flcr.backend.recipe.DTO.request.RecipeListRequestDTO;
import flcr.backend.recipe.DTO.response.RecipeDetailDTO;
import flcr.backend.recipe.DTO.response.RecipeListItemDTO;
import flcr.backend.recipe.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    public Response<Long> publishRecipe(
            @Valid @RequestPart("request") PublishRecipeRequestDTO request,
            @RequestParam("cover") MultipartFile cover,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        Long recipeId = recipeService.publishRecipe(request, cover, images);
        return Response.success(recipeId);
    }

    @Public
    @GetMapping("/list")
    public Response<Page<RecipeListItemDTO>> getRecipeList(RecipeListRequestDTO request) {
        Page<RecipeListItemDTO> result = recipeService.getRecipeList(request);
        return Response.success(result);
    }

    @Public
    @GetMapping("/{id}")
    public Response<RecipeDetailDTO> getRecipeDetail(@PathVariable Long id) {
        RecipeDetailDTO detail = recipeService.getRecipeDetail(id);
        return Response.success(detail);
    }
}
