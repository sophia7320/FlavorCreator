package flcr.backend.recipe.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyRecipeResponseDTO {

    private Integer matchDegree;
    private List<RecipeListItemResponseDTO> recipes;
    private Boolean needAiGenerate;
}
