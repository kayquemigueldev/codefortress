package com.codefortress.dashboard.api;

import com.codefortress.dashboard.DashboardOverview;
import com.codefortress.dashboard.GetDashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final GetDashboardService getDashboardService;

    public DashboardController(
            GetDashboardService getDashboardService
    ) {
        this.getDashboardService = getDashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID ownerId =
                UUID.fromString(
                        jwt.getSubject()
                );

        DashboardOverview overview =
                getDashboardService.get(ownerId);

        return DashboardResponse.from(overview);
    }
}