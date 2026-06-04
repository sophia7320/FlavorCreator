package flcr.backend.common.controller;

import flcr.backend.common.constants.ImageScene;
import flcr.backend.common.response.Response;
import flcr.backend.common.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping("/upload")
    public Response<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("scene") String scene) {
        ImageScene imageScene = ImageScene.fromValue(scene);
        String url = imageUploadService.upload(file, imageScene);
        return Response.success(Map.of("url", url));
    }
}
