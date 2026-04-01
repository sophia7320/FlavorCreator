package flcr.backend.auth;

import flcr.backend.auth.DTO.request.LoginDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {
    final private UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login-ux")
    public String login(@RequestBody LoginDTO request) {
        return userService.login(request);
    }
}
