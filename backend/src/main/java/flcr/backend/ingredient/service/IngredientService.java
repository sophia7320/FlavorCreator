package flcr.backend.ingredient.service;

import flcr.backend.ingredient.DTO.request.IngredientAddRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientBatchAddRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientListRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientUpdateRequestDTO;
import flcr.backend.ingredient.DTO.response.CommonIngredientResponseDTO;
import flcr.backend.ingredient.DTO.response.ExpiringNoticeResponseDTO;
import flcr.backend.ingredient.DTO.response.IngredientListResponseDTO;

import java.util.List;

public interface IngredientService {

    IngredientListResponseDTO list(IngredientListRequestDTO query);

    Long add(IngredientAddRequestDTO request);

    void update(Long id, IngredientUpdateRequestDTO request);

    void delete(Long id);

    List<Long> batchAdd(IngredientBatchAddRequestDTO request);

    ExpiringNoticeResponseDTO expiringNotice();

    CommonIngredientResponseDTO commonList();
}
