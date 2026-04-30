package flcr.backend.ingredient.controller;

import flcr.backend.common.aop.Public;
import flcr.backend.common.response.Response;
import flcr.backend.ingredient.DTO.request.IngredientAddRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientBatchAddRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientListQueryDTO;
import flcr.backend.ingredient.DTO.request.IngredientUpdateRequestDTO;
import flcr.backend.ingredient.DTO.response.CommonIngredientResponseDTO;
import flcr.backend.ingredient.DTO.response.ExpiringNoticeResponseDTO;
import flcr.backend.ingredient.DTO.response.IngredientListResponseDTO;
import flcr.backend.ingredient.service.IngredientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ingredient")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping("/list")
    public Response<IngredientListResponseDTO> list(IngredientListQueryDTO query) {
        return Response.success(ingredientService.list(query));
    }

    @PostMapping
    public Response<Long> add(@RequestBody IngredientAddRequestDTO request) {
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

    @PostMapping("/batch")
    public Response<List<Long>> batchAdd(@RequestBody IngredientBatchAddRequestDTO request) {
        return Response.success(ingredientService.batchAdd(request));
    }

    @GetMapping("/expiring-notice")
    public Response<ExpiringNoticeResponseDTO> expiringNotice() {
        return Response.success(ingredientService.expiringNotice());
    }

    @Public
    @GetMapping("/common")
    public Response<CommonIngredientResponseDTO> commonList() {
        return Response.success(ingredientService.commonList());
    }
}
