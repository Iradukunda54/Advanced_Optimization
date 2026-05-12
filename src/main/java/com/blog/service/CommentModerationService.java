package com.blog.service;

import com.blog.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

/**
 * Service for concurrent comment moderation using ExecutorService and CopyOnWriteArrayList.
 * 
 * Demonstrates:
 * - Parallel processing of a list using a custom ExecutorService
 * - Use of thread-safe collection (CopyOnWriteArrayList) for concurrent modifications
 * - Thread pool management and synchronization
 * 
 * Rationale: Moderating comments (e.g., checking for spam or forbidden words) can be done
 * in parallel for each comment to improve system throughput.
 */
@Service
public class CommentModerationService {

    private static final Logger logger = LoggerFactory.getLogger(CommentModerationService.class);
    
    // Dedicated thread pool for moderation tasks
    private final ExecutorService moderationExecutor = Executors.newFixedThreadPool(4);

    /**
     * Moderates a list of comments in parallel.
     * 
     * @param comments The list of comments to moderate
     * @return A thread-safe list containing comments that passed moderation
     */
    public List<Comment> moderateCommentsConcurrent(List<Comment> comments) {
        logger.info("[Concurrency] Starting parallel moderation for {} comments", comments.size());
        
        // Use CopyOnWriteArrayList for thread-safe additions from multiple threads
        CopyOnWriteArrayList<Comment> approvedComments = new CopyOnWriteArrayList<>();
        
        CountDownLatch latch = new CountDownLatch(comments.size());

        for (Comment comment : comments) {
            moderationExecutor.submit(() -> {
                try {
                    // Simulate moderation logic (regex checks, external AI API call, etc.)
                    boolean isApproved = performModerationLogic(comment);
                    
                    if (isApproved) {
                        approvedComments.add(comment);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            // Wait for all moderation tasks to complete (timeout after 30s)
            if (!latch.await(30, TimeUnit.SECONDS)) {
                logger.warn("[Concurrency] Moderation timeout reached before all comments were processed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[Concurrency] Moderation process interrupted", e);
        }

        logger.info("[Concurrency] Moderation complete. Approved {}/{} comments", 
                approvedComments.size(), comments.size());
        
        return approvedComments;
    }

    private boolean performModerationLogic(Comment comment) {
        // Simulate processing time
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        
        // Simple mock logic: filter out comments containing "spam"
        return !comment.getContent().toLowerCase().contains("spam");
    }
}
