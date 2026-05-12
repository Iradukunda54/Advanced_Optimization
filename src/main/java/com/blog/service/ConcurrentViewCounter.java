package com.blog.service;

import com.blog.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe view counter using ConcurrentHashMap and AtomicInteger.
 * 
 * Demonstrates:
 * - High-concurrency throughput optimization for frequent updates
 * - Use of thread-safe collections (ConcurrentHashMap) and atomic variables (AtomicInteger)
 * - Batching updates to reduce database overhead via @Scheduled flush
 * 
 * Rationale: Directly updating the database for every view increment is slow and creates 
 * transaction overhead under high load. This service buffers views in memory and flushes them 
 * periodically.
 */
@Service
public class ConcurrentViewCounter {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentViewCounter.class);
    
    // Memory-efficient and thread-safe storage for view counts
    private final ConcurrentHashMap<Long, AtomicInteger> viewCache = new ConcurrentHashMap<>();
    
    private final PostRepository postRepository;

    public ConcurrentViewCounter(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Increment view count for a post atomically.
     * Lock-free and highly concurrent.
     */
    public void incrementView(Long postId) {
        viewCache.computeIfAbsent(postId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * Get current view count from memory.
     */
    public int getViewCount(Long postId) {
        AtomicInteger count = viewCache.get(postId);
        return count != null ? count.get() : 0;
    }

    /**
     * Periodic task to flush in-memory view counts to the database.
     * Runs every 1 minute to reduce write pressure on the DB.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void flushToDB() {
        if (viewCache.isEmpty()) return;

        logger.info("[Concurrency] Flushing {} view updates to database...", viewCache.size());
        
        for (Map.Entry<Long, AtomicInteger> entry : viewCache.entrySet()) {
            Long postId = entry.getKey();
            int additionalViews = entry.getValue().getAndSet(0); // Atomic reset to 0
            
            if (additionalViews > 0) {
                postRepository.findById(postId).ifPresent(post -> {
                    post.setViews(post.getViews() + additionalViews);
                    postRepository.save(post);
                });
            }
        }
        
        // Remove entries with 0 views to save memory
        viewCache.entrySet().removeIf(entry -> entry.getValue().get() == 0);
        logger.info("[Concurrency] Flush completed.");
    }
}
