package com.pennywise.dashboard;

import com.pennywise.common.ApiResponse;
import com.pennywise.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDTO.DashboardResponse>> getDashboard(
            @AuthenticatedUser Long userId) {
        DashboardDTO.DashboardResponse response = dashboardService.getDashboardData(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard data retrieved"));
    }
}
