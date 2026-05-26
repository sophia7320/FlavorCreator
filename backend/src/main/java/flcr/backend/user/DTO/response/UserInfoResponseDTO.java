package flcr.backend.user.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDTO {
    private Long id;
    private String openid;
    private String nickname;
    private String avatar;
    private String background;
    private String signature;
    private Integer gender;
    private String phone;
    private PreferencesInfo preferences;
    private StatsInfo stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferencesInfo {
        private List<String> taste;
        private List<String> dietary;
        private String cookTime;
        private String difficulty;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsInfo {
        private Integer followingCount;
        private Integer followerCount;
        private Integer likeCount;
        private Integer collectionCount;
    }
}
