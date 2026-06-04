package flcr.backend.ingredient.controller;

import flcr.backend.ingredient.DTO.request.IngredientAddRequestDTO;
import flcr.backend.ingredient.DTO.request.IngredientListRequestDTO;
import flcr.backend.ingredient.DTO.response.IngredientListResponseDTO;
import flcr.backend.ingredient.service.IngredientService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("调味品控制器测试")
class CondimentControllerTest {

    @Mock private IngredientService ingredientService;
    @InjectMocks private CondimentController controller;

    @Test
    @DisplayName("list设置category为调味品")
    void testList_SetsCategoryToCondiment() {
        IngredientListRequestDTO query = new IngredientListRequestDTO();
        when(ingredientService.list(any())).thenReturn(IngredientListResponseDTO.builder().build());

        assertEquals(200, controller.list(query).getCode());
        assertEquals("调味品", query.getCategory());
    }

    @Test
    @DisplayName("add设置category为调味品")
    void testAdd_SetsCategoryToCondiment() {
        IngredientAddRequestDTO req = new IngredientAddRequestDTO();
        when(ingredientService.add(req)).thenReturn(1L);

        controller.add(req);
        assertEquals("调味品", req.getCategory());
    }

    @Test
    @DisplayName("add返回id")
    void testAdd_ReturnsId() {
        IngredientAddRequestDTO req = new IngredientAddRequestDTO();
        when(ingredientService.add(req)).thenReturn(5L);

        assertEquals(5L, controller.add(req).getData());
    }
}
