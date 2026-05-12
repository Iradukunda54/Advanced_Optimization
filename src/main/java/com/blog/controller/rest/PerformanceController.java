package com.blog.controller.rest;

import com.blog.dto.ApiResponse;
import com.blog.service.PerformanceMetricsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller to expose performance metrics and comparison reports.
 */
@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceMetricsService metricsService;

    public PerformanceController(PerformanceMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @PostMapping("/baseline")
    public ApiResponse<String> captureBaseline() {
        metricsService.captureSnapshot("baseline");
        return ApiResponse.success("Baseline snapshot captured", null);
    }

    @PostMapping("/optimized")
    public ApiResponse<String> captureOptimized() {
        metricsService.captureSnapshot("optimized");
        return ApiResponse.success("Optimized snapshot captured", null);
    }

    @GetMapping("/report")
    public ApiResponse<Map<String, PerformanceMetricsService.PerformanceSnapshot>> getReport() {
        return ApiResponse.success("Performance snapshots retrieved", metricsService.getAllSnapshots());
    }

    @GetMapping("/comparison")
    public ApiResponse<Map<String, Object>> getComparison(
            @RequestParam(defaultValue = "baseline") String baseline,
            @RequestParam(defaultValue = "optimized") String optimized) {
        return ApiResponse.success("Performance comparison generated", 
                metricsService.generateComparison(baseline, optimized));
    }
}
