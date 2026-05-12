package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async and scheduling configuration for the blogging platform.
 * 
 * Configures custom thread pools for different workload types:
 * - blogTaskExecutor: General-purpose async tasks (post publishing, feed aggregation)
 * - analyticsExecutor: CPU-intensive analytics and reporting operations
 * - notificationExecutor: High-throughput, low-latency notification dispatch
 * 
 * Thread pool sizing rationale:
 * - Core pool size = number of CPU cores for CPU-bound work
 * - Max pool size = 2x core for I/O-bound work to handle blocking operations
 * - Queue capacity tuned per workload: higher for notifications (burst-tolerant)
 */
@Configuration
@EnableScheduling
public class AsyncConfig {

    /**
     * General-purpose thread pool for blog operations.
     * Core=4, Max=8: balanced for mixed I/O and CPU workloads.
     * Queue=100: moderate buffering for post publishing bursts.
     */
    @Bean(name = "blogTaskExecutor")
    public Executor blogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("BlogTask-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated thread pool for analytics computations.
     * Core=2, Max=4: analytics are periodic, not latency-critical.
     * Queue=50: smaller queue to prevent backlog accumulation.
     */
    @Bean(name = "analyticsExecutor")
    public Executor analyticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Analytics-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * High-throughput thread pool for notification dispatch.
     * Core=2, Max=4: notifications are I/O-bound (simulated sends).
     * Queue=200: large queue to absorb notification bursts without rejection.
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        executor.initialize();
        return executor;
    }
}
