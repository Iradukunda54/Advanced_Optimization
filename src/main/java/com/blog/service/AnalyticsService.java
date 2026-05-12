package com.blog.service;

import com.blog.dto.CommentDTO;
import com.blog.dto.PostDTO;
import com.blog.dto.UserDTO;
import com.blog.model.Post;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Asynchronous analytics service using CompletableFuture and @Async.
 * 
 * Demonstrates:
 * - @Async with dedicated analyticsExecutor thread pool
 * - CompletableFuture.allOf() for parallel aggregation of multiple data sources
 * - CompletableFuture.supplyAsync() for individual async computations
 * 
 * Operations like analytics aggregation, feed generation, and user activity
 * reports are inherently parallelizable since they read from independent data sources.
 */
@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public AnalyticsService(PostRepository postRepository, CommentRepository commentRepository,
                            UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Calculate analytics for a single post asynchronously.
     * Aggregates view count, comment count, and average review rating in parallel.
     */
    @Async("analyticsExecutor")
    public CompletableFuture<Map<String, Object>> calculatePostAnalytics(Long postId) {
        long startTime = System.currentTimeMillis();
        logger.info("[Analytics] Calculating post analytics for postId={} on thread: {}", 
                postId, Thread.currentThread().getName());

        // Parallel computation of post metrics
        CompletableFuture<Long> commentCountFuture = CompletableFuture.supplyAsync(() -> {
            List<?> comments = commentRepository.findByPostId(postId);
            return (long) comments.size();
        });

        CompletableFuture<Optional<Post>> postFuture = CompletableFuture.supplyAsync(() ->
                postRepository.findById(postId));

        // Wait for all futures to complete
        CompletableFuture.allOf(commentCountFuture, postFuture).join();

        Map<String, Object> analytics = new LinkedHashMap<>();
        try {
            Optional<Post> post = postFuture.get();
            if (post.isPresent()) {
                Post p = post.get();
                analytics.put("postId", postId);
                analytics.put("title", p.getTitle());
                analytics.put("views", p.getViews());
                analytics.put("commentCount", commentCountFuture.get());
                analytics.put("createdAt", p.getCreatedAt().toString());
            }
        } catch (Exception e) {
            logger.error("[Analytics] Error computing post analytics", e);
            analytics.put("error", e.getMessage());
        }

        long duration = System.currentTimeMillis() - startTime;
        analytics.put("computationTimeMs", duration);
        logger.info("[Analytics] Post analytics completed in {} ms", duration);

        return CompletableFuture.completedFuture(analytics);
    }

    /**
     * Generate user activity report asynchronously.
     * Fetches user details, post count, and comment count in parallel using CompletableFuture.
     */
    @Async("analyticsExecutor")
    public CompletableFuture<Map<String, Object>> generateUserActivityReport(Long userId) {
        long startTime = System.currentTimeMillis();
        logger.info("[Analytics] Generating user activity report for userId={} on thread: {}", 
                userId, Thread.currentThread().getName());

        CompletableFuture<Long> postCountFuture = CompletableFuture.supplyAsync(() ->
                postRepository.findByAuthorId(userId).stream().count());

        CompletableFuture<Integer> totalViewsFuture = CompletableFuture.supplyAsync(() ->
                postRepository.findByAuthorId(userId).stream()
                        .mapToInt(Post::getViews)
                        .sum());

        CompletableFuture.allOf(postCountFuture, totalViewsFuture).join();

        Map<String, Object> report = new LinkedHashMap<>();
        try {
            report.put("userId", userId);
            report.put("totalPosts", postCountFuture.get());
            report.put("totalViews", totalViewsFuture.get());
        } catch (Exception e) {
            logger.error("[Analytics] Error computing user report", e);
            report.put("error", e.getMessage());
        }

        long duration = System.currentTimeMillis() - startTime;
        report.put("computationTimeMs", duration);
        logger.info("[Analytics] User report completed in {} ms", duration);

        return CompletableFuture.completedFuture(report);
    }

    /**
     * Aggregate a blog feed by fetching trending posts, recent posts, 
     * and total statistics in parallel using CompletableFuture.allOf().
     * 
     * This demonstrates parallel aggregation from multiple independent data sources.
     */
    @Async("analyticsExecutor")
    public CompletableFuture<Map<String, Object>> aggregateBlogFeed() {
        long startTime = System.currentTimeMillis();
        logger.info("[Analytics] Aggregating blog feed on thread: {}", Thread.currentThread().getName());

        // Launch 3 independent queries in parallel
        CompletableFuture<List<String>> trendingFuture = CompletableFuture.supplyAsync(() ->
                postRepository.findTopPopularPosts(org.springframework.data.domain.PageRequest.of(0, 5))
                        .stream()
                        .map(p -> p.getTitle() + " (" + p.getViews() + " views)")
                        .collect(Collectors.toList()));

        CompletableFuture<Long> totalPostsFuture = CompletableFuture.supplyAsync(() ->
                postRepository.count());

        CompletableFuture<Long> totalUsersFuture = CompletableFuture.supplyAsync(() ->
                userRepository.count());

        // Wait for all three to complete
        CompletableFuture.allOf(trendingFuture, totalPostsFuture, totalUsersFuture).join();

        Map<String, Object> feed = new LinkedHashMap<>();
        try {
            feed.put("trendingPosts", trendingFuture.get());
            feed.put("totalPosts", totalPostsFuture.get());
            feed.put("totalUsers", totalUsersFuture.get());
        } catch (Exception e) {
            logger.error("[Analytics] Error aggregating feed", e);
            feed.put("error", e.getMessage());
        }

        long duration = System.currentTimeMillis() - startTime;
        feed.put("aggregationTimeMs", duration);
        logger.info("[Analytics] Feed aggregation completed in {} ms", duration);

        return CompletableFuture.completedFuture(feed);
    }
}
