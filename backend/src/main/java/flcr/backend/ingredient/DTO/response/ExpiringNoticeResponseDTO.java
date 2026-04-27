package flcr.backend.ingredient.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpiringNoticeResponseDTO {
    private List<ExpiringItem> expiring;
    private List<ExpiringItem> expired;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpiringItem {
        private Long id;
        private String name;
        private LocalDate expireDate;
        private Long daysLeft;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer expiringCount;
        private Integer expiredCount;
    }
}
