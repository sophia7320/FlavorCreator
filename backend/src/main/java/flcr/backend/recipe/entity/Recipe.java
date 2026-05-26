package flcr.backend.recipe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜谱实体类
 */
@Data
@TableName(value = "`recipe`", autoResultMap = true)
public class Recipe {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 菜谱名称
     */
    private String name;

    /**
     * 封面图
     */
    private String cover;

    /**
     * 图片列表（JSON）
     */
    private String images;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 食材列表（JSON）
     */
    private String ingredients;

    /**
     * 步骤列表（JSON）
     */
    private String steps;

    /**
     * 小贴士
     */
    private String tips;

    /**
     * 烹饪方式：简单/普通/慢炖
     */
    private String cookTime;

    /**
     * 难度：1-简单 2-中等 3-困难
     */
    private Integer difficulty;

    /**
     * 卡路里
     */
    private Integer calories;

    /**
     * 标签（JSON）
     */
    private String tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 来源：1-系统 2-用户 3-AI
     */
    private Integer source;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer collectionCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
