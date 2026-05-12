package com.blog.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service to capture and compare performance metrics.
 * 
 * Demonstrates:
 * - Integration with Micrometer for real-time monitoring
 * - Capturing performance snapshots (latency, memory, CPU)
 * - Side-by-side comparison of baseline vs optimized performance
 */
@Service
public class PerformanceMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsService.class);

    private final MeterRegistry meterRegistry;
    
    // Store snapshots for comparison
    private final Map<String, PerformanceSnapshot> snapshots = new ConcurrentHashMap<>();

    public PerformanceMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Capture a performance snapshot.
     * 
     * @param label A unique label for the snapshot (e.g., "baseline", "optimized")
     */
    public void captureSnapshot(String label) {
        logger.info("[Metrics] Capturing performance snapshot: {}", label);
        
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024); // MB
        
        PerformanceSnapshot snapshot = new PerformanceSnapshot();
        snapshot.timestamp = new Date();
        snapshot.memoryUsedMb = usedMemory;
        snapshot.activeThreads = Thread.activeCount();
        
        // Extract latencies from Micrometer timers
        Map<String, Double> latencies = new HashMap<>();
        meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith("blog.api.response.time"))
                .forEach(meter -> {
                    Timer timer = (Timer) meter;
                    latencies.put(meter.getId().getTag("uri"), timer.mean(TimeUnit.MILLISECONDS));
                });
        
        snapshot.averageLatencies = latencies;
        snapshots.put(label, snapshot);
    }

    /**
     * Generate a comparison report between two snapshots.
     */
    public Map<String, Object> generateComparison(String baselineLabel, String optimizedLabel) {
        PerformanceSnapshot baseline = snapshots.get(baselineLabel);
        PerformanceSnapshot optimized = snapshots.get(optimizedLabel);
        
        if (baseline == null || optimized == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "One or both snapshots missing. Capture baseline and optimized first.");
            return error;
        }

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("baselineLabel", baselineLabel);
        comparison.put("optimizedLabel", optimizedLabel);
        
        Map<String, Object> resourceDiff = new LinkedHashMap<>();
        resourceDiff.put("memoryImprovementMb", baseline.memoryUsedMb - optimized.memoryUsedMb);
        resourceDiff.put("threadCountChange", optimized.activeThreads - baseline.activeThreads);
        comparison.put("resourceComparison", resourceDiff);

        Map<String, String> latencyImprovement = new LinkedHashMap<>();
        baseline.averageLatencies.forEach((uri, baselineLat) -> {
            Double optimizedLat = optimized.averageLatencies.get(uri);
            if (optimizedLat != null) {
                double pct = ((baselineLat - optimizedLat) / baselineLat) * 100;
                latencyImprovement.put(uri, String.format("%.2f%% improvement (%.2fms -> %.2fms)", 
                        pct, baselineLat, optimizedLat));
            }
        });
        comparison.put("latencyImprovements", latencyImprovement);

        return comparison;
    }

    public Map<String, PerformanceSnapshot> getAllSnapshots() {
        return snapshots;
    }

    public static class PerformanceSnapshot {
        public Date timestamp;
        public long memoryUsedMb;
        public int activeThreads;
        public Map<String, Double> averageLatencies; // URI -> Mean Latency
    }
}
