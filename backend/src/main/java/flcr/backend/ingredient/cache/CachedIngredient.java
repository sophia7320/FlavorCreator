package flcr.backend.ingredient.cache;

import flcr.backend.ingredient.entity.Ingredient;
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
public class CachedIngredient {

    private Long id;

    private Long userId;

    private String name;

    private BigDecimal quantity;

    private String unit;

    private String category;

    private String storageCondition;

    private LocalDate expireDate;

    private LocalDateTime createdAt;

    private Boolean readed;

    private Integer status;

    public static CachedIngredient from(Ingredient ingredient, Integer status) {
        return CachedIngredient.builder()
                .id(ingredient.getId())
                .userId(ingredient.getUserId())
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .unit(ingredient.getUnit())
                .category(ingredient.getCategory())
                .storageCondition(ingredient.getStorageCondition())
                .expireDate(ingredient.getExpireDate())
                .createdAt(ingredient.getCreatedAt())
                .readed(false)
                .status(status)
                .build();
    }
}
