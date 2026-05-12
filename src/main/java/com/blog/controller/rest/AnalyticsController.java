package com.blog.controller.rest;

import com.blog.dto.ApiResponse;
import com.blog.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for asynchronous analytics.
 * 
 * Demonstrates:
 * - Returning CompletableFuture from a controller for non-blocking I/O
 * - Exposing async-calculated data for posts, users, and the blog feed
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/post/{id}")
    public CompletableFuture<ApiResponse<Map<String, Object>>> getPostAnalytics(@PathVariable Long id) {
        return analyticsService.calculatePostAnalytics(id)
                .thenApply(data -> ApiResponse.success("Post analytics calculated", data));
    }

    @GetMapping("/user/{id}")
    public CompletableFuture<ApiResponse<Map<String, Object>>> getUserReport(@PathVariable Long id) {
        return analyticsService.generateUserActivityReport(id)
                .thenApply(data -> ApiResponse.success("User activity report generated", data));
    }

    @GetMapping("/feed")
    public CompletableFuture<ApiResponse<Map<String, Object>>> getBlogFeed() {
        return analyticsService.aggregateBlogFeed()
                .thenApply(data -> ApiResponse.success("Blog feed aggregated", data));
    }
}
