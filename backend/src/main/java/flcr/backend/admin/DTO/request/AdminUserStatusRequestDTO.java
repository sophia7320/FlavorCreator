package flcr.backend.admin.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserStatusRequestDTO {
    @NotBlank(message = "状态不能为空")
    private String status;
}
