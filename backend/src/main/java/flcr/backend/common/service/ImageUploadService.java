package flcr.backend.common.service;

import flcr.backend.common.constants.ImageScene;
import org.springframework.web.multipart.MultipartFile;

/**
 * 统一图片上传服务。封装 validate → store → moderate 三步流程，
 * 失败时自动清理已存储文件。
 */
public interface ImageUploadService {

    /**
     * 上传图片。
     * @param file  图片文件
     * @param scene 上传场景
     * @return 图片访问 URL
     */
    String upload(MultipartFile file, ImageScene scene);
}
