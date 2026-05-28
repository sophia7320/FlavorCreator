package flcr.backend.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.common.response.Response;
import flcr.backend.recipe.DTO.response.RecipeListItemDTO;
import flcr.backend.user.DTO.response.MyCollectionResponseDTO;
import flcr.backend.user.DTO.response.MyLikeResponseDTO;
import flcr.backend.user.service.UserCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserCenterController {

    private final UserCenterService userCenterService;

    @GetMapping("/collections")
    public Response<Page<MyCollectionResponseDTO>> getCollections(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<MyCollectionResponseDTO> result = userCenterService.getMyCollections(page, size);
        return Response.success(result);
    }

    @GetMapping("/likes")
    public Response<Page<MyLikeResponseDTO>> getLikes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<MyLikeResponseDTO> result = userCenterService.getMyLikes(page, size);
        return Response.success(result);
    }

    @GetMapping("/recipes")
    public Response<Page<RecipeListItemDTO>> getRecipes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<RecipeListItemDTO> result = userCenterService.getMyRecipes(page, size);
        return Response.success(result);
    }
}
