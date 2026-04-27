package flcr.backend.auth.DTO.request;

import lombok.Data;

@Data
public class LoginDTO {
    private String code;

    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private String nickName;
        private String avatarUrl;
        private String gender;
    }
}
