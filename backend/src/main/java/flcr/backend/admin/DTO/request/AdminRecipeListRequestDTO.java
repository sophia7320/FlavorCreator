package flcr.backend.admin.DTO.request;

import lombok.Data;

@Data
public class AdminRecipeListRequestDTO {
    private Integer page = 1;
    private Integer size = 20;
    private String keyword;
    private Integer source;
}
