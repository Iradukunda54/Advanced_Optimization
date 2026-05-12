package com.blog.service;

import com.blog.model.Comment;
import com.blog.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Asynchronous notification dispatch service using @Async.
 * 
 * Demonstrates:
 * - Fire-and-forget asynchronous execution using @Async
 * - Use of dedicated notificationExecutor thread pool to isolate notification latency from the main API thread
 * 
 * In a real application, this would integrate with an Email or Push notification service.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Notify about a new post asynchronously.
     * Uses notificationExecutor thread pool.
     */
    @Async("notificationExecutor")
    public void notifyOnNewPost(Post post) {
        logger.info("[Notification] Processing new post notification: '{}' by {}", 
                post.getTitle(), post.getAuthor().getUsername());
        
        try {
            // Simulate notification delay (e.g., calling external SMTP or SMS API)
            Thread.sleep(2000); 
            logger.info("[Notification] Successfully sent notification for post: {}", post.getTitle());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[Notification] Error sending post notification", e);
        }
    }

    /**
     * Notify about a new comment asynchronously.
     * Uses notificationExecutor thread pool.
     */
    @Async("notificationExecutor")
    public void notifyOnNewComment(Comment comment) {
        logger.info("[Notification] Processing new comment notification on post: '{}' by {}", 
                comment.getPost().getTitle(), comment.getAuthor().getUsername());
        
        try {
            // Simulate notification delay
            Thread.sleep(1000);
            logger.info("[Notification] Successfully sent notification for comment on post: {}", 
                    comment.getPost().getTitle());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[Notification] Error sending comment notification", e);
        }
    }
}
