package flcr.backend.user.controller;

import flcr.backend.common.aop.RequireAuth;
import flcr.backend.common.response.Response;
import flcr.backend.user.DTO.request.UpdateUserInfoRequestDTO;
import flcr.backend.user.DTO.response.UserInfoResponseDTO;
import flcr.backend.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    @RequireAuth
    @GetMapping("/info")
    public Response<UserInfoResponseDTO> getInfo() {
        return Response.success(userInfoService.getInfo());
    }

    @RequireAuth
    @PostMapping("/info")
    public Response<UserInfoResponseDTO> updateInfo(@RequestBody UpdateUserInfoRequestDTO request) {
        return Response.success(userInfoService.updateInfo(request));
    }

    @RequireAuth
    @PostMapping("/avatar")
    public Response<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userInfoService.uploadAvatar(file);
        return Response.success(Map.of("avatarUrl", avatarUrl));
    }

    @RequireAuth
    @PostMapping("/background")
    public Response<Map<String, String>> uploadBackground(@RequestParam("file") MultipartFile file) {
        String backgroundUrl = userInfoService.uploadBackground(file);
        return Response.success(Map.of("backgroundUrl", backgroundUrl));
    }
}
