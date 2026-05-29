package flcr.backend.admin.controller;

import flcr.backend.admin.DTO.response.AdminStatsResponseDTO;
import flcr.backend.admin.service.AdminStatsService;
import flcr.backend.common.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    public Response<AdminStatsResponseDTO> overview() {
        AdminStatsResponseDTO result = adminStatsService.getOverview();
        return Response.success(result);
    }
}
