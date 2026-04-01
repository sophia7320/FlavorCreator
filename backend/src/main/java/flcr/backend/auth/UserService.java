package flcr.backend.auth;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import flcr.backend.auth.DTO.request.LoginDTO;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper,User> {
    public String login(LoginDTO request) {

        return "token";
    }
}
