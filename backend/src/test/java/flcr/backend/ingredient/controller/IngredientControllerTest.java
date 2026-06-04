package flcr.backend.ingredient.controller;

import flcr.backend.ingredient.DTO.request.*;
import flcr.backend.ingredient.DTO.response.*;
import flcr.backend.ingredient.service.IngredientService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("食材控制器测试")
class IngredientControllerTest {

    @Mock private IngredientService ingredientService;
    @InjectMocks private IngredientController controller;

    @Test
    @DisplayName("list返回食材列表")
    void testList_ReturnsList() {
        IngredientListRequestDTO query = new IngredientListRequestDTO();
        IngredientListResponseDTO rsp = IngredientListResponseDTO.builder().build();
        when(ingredientService.list(query)).thenReturn(rsp);

        assertEquals(200, controller.list(query).getCode());
    }

    @Test
    @DisplayName("add返回id")
    void testAdd_ReturnsId() {
        IngredientAddRequestDTO req = new IngredientAddRequestDTO();
        when(ingredientService.add(req)).thenReturn(1L);

        assertEquals(1L, controller.add(req).getData());
    }

    @Test
    @DisplayName("update成功")
    void testUpdate_Success() {
        IngredientUpdateRequestDTO req = new IngredientUpdateRequestDTO();
        assertDoesNotThrow(() -> controller.update(1L, req));
        verify(ingredientService).update(1L, req);
    }

    @Test
    @DisplayName("delete成功")
    void testDelete_Success() {
        assertDoesNotThrow(() -> controller.delete(1L));
        verify(ingredientService).delete(1L);
    }

    @Test
    @DisplayName("batchAdd返回ids")
    void testBatchAdd_ReturnsIds() {
        IngredientBatchAddRequestDTO req = new IngredientBatchAddRequestDTO();
        when(ingredientService.batchAdd(req)).thenReturn(List.of(1L, 2L));

        assertEquals(2, controller.batchAdd(req).getData().size());
    }

    @Test
    @DisplayName("expiringNotice返回提醒")
    void testExpiringNotice_ReturnsNotice() {
        when(ingredientService.expiringNotice()).thenReturn(ExpiringNoticeResponseDTO.builder().build());
        assertEquals(200, controller.expiringNotice().getCode());
    }

    @Test
    @DisplayName("commonList返回常用食材")
    void testCommonList_ReturnsList() {
        when(ingredientService.commonList()).thenReturn(CommonIngredientResponseDTO.builder().build());
        assertEquals(200, controller.commonList().getCode());
    }
}
