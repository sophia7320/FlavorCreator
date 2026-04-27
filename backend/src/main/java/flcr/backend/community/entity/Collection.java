package flcr.backend.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏实体类
 */
@Data
@TableName("collection")
public class Collection {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 菜谱ID
     */
    private Long recipeId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
