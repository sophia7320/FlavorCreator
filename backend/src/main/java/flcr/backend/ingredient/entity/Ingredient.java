package flcr.backend.ingredient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("`ingredient`")
public class Ingredient {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private BigDecimal quantity;

    private String unit;

    private String category;

    private String storageCondition;

    private LocalDate expireDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
