package flcr.backend.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(autoResultMap=true)
@Data
public class User {
    @TableId(type = IdType.AUTO , value = "id")
    int id;

    int gender;

    String nickname;
    String phoneNumber;

    String openid;
    String unionid;

    String avatar;
    String signature;
    String background;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;



}
