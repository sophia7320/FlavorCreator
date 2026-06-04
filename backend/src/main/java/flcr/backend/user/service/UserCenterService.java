package flcr.backend.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flcr.backend.recipe.DTO.response.RecipeListItemResponseDTO;
import flcr.backend.user.DTO.response.MyCollectionResponseDTO;
import flcr.backend.user.DTO.response.MyLikeResponseDTO;

public interface UserCenterService {

    Page<MyCollectionResponseDTO> getMyCollections(Integer page, Integer size);

    Page<MyLikeResponseDTO> getMyLikes(Integer page, Integer size);

    Page<RecipeListItemResponseDTO> getMyRecipes(Integer page, Integer size);
}
