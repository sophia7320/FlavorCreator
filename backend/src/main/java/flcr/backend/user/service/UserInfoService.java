package flcr.backend.user.service;

import flcr.backend.user.DTO.request.UpdateUserInfoRequestDTO;
import flcr.backend.user.DTO.response.UserInfoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserInfoService {

    UserInfoResponseDTO getInfo();

    UserInfoResponseDTO updateInfo(UpdateUserInfoRequestDTO request);

    String uploadAvatar(MultipartFile file);

    String uploadBackground(MultipartFile file);
}
