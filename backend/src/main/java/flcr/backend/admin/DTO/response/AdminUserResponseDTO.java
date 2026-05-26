package flcr.backend.admin.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponseDTO {
    private Long id;
    private String nickname;
    private String avatar;
    private String phoneNumber;
    private Integer gender;
    private String status;
    private String createdAt;
    private String updatedAt;
}
