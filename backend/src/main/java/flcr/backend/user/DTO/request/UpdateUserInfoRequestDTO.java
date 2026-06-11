package flcr.backend.user.DTO.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UpdateUserInfoRequestDTO {
    @Size(max = 30, message = "昵称最长30字")
    private String nickname;

    @Size(max = 100, message = "个性签名最长100字")
    private String signature;

    private String background;

    @Min(value = 0, message = "性别取值0-2")
    @Max(value = 2, message = "性别取值0-2")
    private Integer gender;
    private String avatar;

    @Size(max = 100, message = "地区最长100字")
    private String address;

    @Min(value = 0, message = "年龄不能为负数")
    @Max(value = 150, message = "年龄取值0-150")
    private Integer age;

    private Preferences preferences;

    @Data
    @NoArgsConstructor
    public static class Preferences {
        private List<String> taste;
        private List<String> dietary;
        private String cookTime;
        private String difficulty;
    }
}
