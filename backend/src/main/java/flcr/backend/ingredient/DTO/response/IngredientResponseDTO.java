package flcr.backend.ingredient.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientResponseDTO {
    private Long id;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private String category;
    private String storageCondition;
    private LocalDate expireDate;
    private Long daysLeft;
    private Integer status;
    private Boolean readed;
    private LocalDateTime createdAt;
}
