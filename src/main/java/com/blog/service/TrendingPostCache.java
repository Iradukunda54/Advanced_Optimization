package com.blog.service;

import com.blog.dto.PostDTO;
import com.blog.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory cache for trending posts using DSA optimization (PriorityQueue).
 * 
 * Demonstrates:
 * - Use of PriorityQueue (Min-Heap) for efficient Top-K selection (O(N log K))
 * - ConcurrentHashMap for thread-safe caching
 * - Algorithmic optimization over standard O(N log N) sorting
 */
@Service
public class TrendingPostCache {

    private static final Logger logger = LoggerFactory.getLogger(TrendingPostCache.class);
    
    // In-memory storage for the current top trending posts
    private final Map<Integer, List<PostDTO>> trendingStore = new ConcurrentHashMap<>();
    
    private final PostRepository postRepository;

    public TrendingPostCache(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Get top-K trending posts from cache.
     */
    public List<PostDTO> getTrendingPosts(int limit) {
        return trendingStore.getOrDefault(limit, Collections.emptyList());
    }

    /**
     * Periodically refresh the trending posts cache using an optimized Min-Heap algorithm.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000)
    public void refreshTrendingCache() {
        logger.info("[DSA] Refreshing trending posts cache...");
        int limit = 10; // We cache the top 10
        
        List<PostDTO> allPosts = postRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        // DSA Optimization: Use a Min-PriorityQueue to find Top-K elements in O(N log K)
        // Instead of sorting everything in O(N log N)
        PriorityQueue<PostDTO> minHeap = new PriorityQueue<>(Comparator.comparingInt(PostDTO::getViews));
        
        for (PostDTO post : allPosts) {
            minHeap.offer(post);
            if (minHeap.size() > limit) {
                minHeap.poll(); // Remove the element with the smallest view count
            }
        }
        
        // Extract from heap and sort in descending order
        List<PostDTO> topPosts = new ArrayList<>(minHeap);
        topPosts.sort(Comparator.comparingInt(PostDTO::getViews).reversed());
        
        trendingStore.put(limit, topPosts);
        trendingStore.put(5, topPosts.subList(0, Math.min(5, topPosts.size())));
        
        logger.info("[DSA] Trending cache refreshed. Top post: '{}' with {} views", 
                topPosts.isEmpty() ? "None" : topPosts.get(0).getTitle(),
                topPosts.isEmpty() ? 0 : topPosts.get(0).getViews());
    }

    private PostDTO mapToDTO(com.blog.model.Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setViews(post.getViews());
        dto.setAuthorName(post.getAuthor().getUsername());
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }
}
