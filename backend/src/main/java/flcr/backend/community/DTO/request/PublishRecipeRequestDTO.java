package flcr.backend.community.DTO.request;

import lombok.Data;

/**
 * 发布菜谱请求DTO
 */
@Data
public class PublishRecipeRequestDTO {
    /**
     * 菜谱名称
     */
    private String name;

    /**
     * 食材列表（JSON字符串）
     */
    private String ingredients;

    /**
     * 步骤列表（JSON字符串）
     */
    private String steps;

    /**
     * 标签（JSON字符串）
     */
    private String tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 小贴士
     */
    private String tips;

    /**
     * 烹饪时间
     */
    private Integer cookTime;

    /**
     * 难度
     */
    private Integer difficulty;

    /**
     * 卡路里
     */
    private Integer calories;
}
