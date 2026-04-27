package flcr.backend.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞实体类
 */
@Data
@TableName("`like`")
public class Like {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类型：1-菜谱 2-评论
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
