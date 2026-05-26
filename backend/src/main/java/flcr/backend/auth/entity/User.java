package flcr.backend.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(value = "`user`", autoResultMap = true)
@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer gender;

    private String nickname;
    private String phoneNumber;

    private String openid;
    private String unionid;

    private String avatar;
    private String signature;
    private String background;
    private String preferences;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
