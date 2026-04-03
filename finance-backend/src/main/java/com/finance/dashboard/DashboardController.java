package com.finance.dashboard;

import com.finance.dto.response.DashboardSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Dashboard", description = "Analytics and summary endpoints for the finance dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Full dashboard summary: totals, category breakdown, trends, recent activity (ALL roles)")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(defaultValue = "10") int recentCount) {
        return ResponseEntity.ok(dashboardService.getFullSummary(recentCount));
    }

    @Operation(summary = "Category-wise totals (ALL roles)")
    @GetMapping("/by-category")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryBreakdown() {
        return ResponseEntity.ok(dashboardService.getCategoryBreakdown());
    }

    @Operation(summary = "Recent N transactions (ALL roles)")
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<DashboardSummaryResponse.RecentActivity>> getRecentActivity(
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(dashboardService.getRecentActivity(count));
    }

    @Operation(summary = "Monthly trends for last N months (ANALYST and ADMIN only)")
    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<List<DashboardSummaryResponse.MonthlyTrend>> getMonthlyTrends(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends(months));
    }
}
