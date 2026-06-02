package flcr.backend.recipe.controller;

import flcr.backend.recipe.DTO.request.Recipe_Request;
import flcr.backend.recipe.DTO.response.Recipe_Response; 
import flcr.backend.recipe.service.Recipe_Service;
import flcr.backend.recipe.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class Recipe_Controller {
    
    private final Recipe_Service recipe_service;

    public Recipe_Controller(Recipe_Service recipe_service) {
        this.recipe_service = recipe_service;
    }

    @PostMapping("/generate")
    public Result<Recipe_Response> handle_recipe_request(@RequestBody Recipe_Request request) {
        try {
            Recipe_Response response_data = recipe_service.generate_recipe(request);
            
            return Result.success(response_data); 
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("菜谱生成失败：" + e.getMessage());
        }
    }
}
