package flcr.backend.user.DTO.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class UpdateUserInfoRequestDTO {
    private String nickname;
    private String signature;
    private String background;
    private Integer gender;
    private Preferences preferences;

    @Data
    @NoArgsConstructor
    public static class Preferences {
        private List<String> taste;
        private List<String> dietary;
        private Integer cookTime;
        private String difficulty;
    }
}
