package flcr.backend.ingredient.controller;

import flcr.backend.common.response.Response;
import flcr.backend.ingredient.DTO.request.IngredientAddRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientListQueryDTO;
import flcr.backend.ingredient.DTO.request.IngredientUpdateRequestDTO;
import flcr.backend.ingredient.DTO.response.IngredientListResponseDTO;
import flcr.backend.ingredient.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/condiment")
@RequiredArgsConstructor
public class CondimentController {

    private static final String CATEGORY_CONDIMENT = "调味品";

    private final IngredientService ingredientService;

    @GetMapping("/list")
    public Response<IngredientListResponseDTO> list(IngredientListQueryDTO query) {
        query.setCategory(CATEGORY_CONDIMENT);
        return Response.success(ingredientService.list(query));
    }

    @PostMapping
    public Response<Long> add(@Valid @RequestBody IngredientAddRequestDTO request) {
        request.setCategory(CATEGORY_CONDIMENT);
        return Response.success(ingredientService.add(request));
    }

    @PutMapping("/{id}")
    public Response<Void> update(@PathVariable Long id, @RequestBody IngredientUpdateRequestDTO request) {
        ingredientService.update(id, request);
        return Response.success();
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        ingredientService.delete(id);
        return Response.success();
    }
}
