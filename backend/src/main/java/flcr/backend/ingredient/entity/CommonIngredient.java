package flcr.backend.ingredient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("`common_ingredient`")
public class CommonIngredient {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String category;

    private String name;

    private String defaultUnit;
}
