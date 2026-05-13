package com.blog.service;

import com.blog.dto.PostDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for search result caching and algorithmic optimization.
 * 
 * Demonstrates:
 * - In-memory search result caching with TTL (Time To Live)
 * - Use of ConcurrentHashMap for thread-safe search result storage
 * - Hash-based deduplication
 */
@Service
public class SearchOptimizationService {

    private static final Logger logger = LoggerFactory.getLogger(SearchOptimizationService.class);

    // Cache to store search results for frequent keywords
    private final ConcurrentHashMap<String, CachedSearchResult> searchCache = new ConcurrentHashMap<>();
    
    private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(10);

    /**
     * Get cached search results if available and not expired.
     */
    public List<PostDTO> getCachedResults(String keyword) {
        CachedSearchResult cached = searchCache.get(keyword.toLowerCase());
        
        if (cached != null) {
            if (System.currentTimeMillis() - cached.timestamp < CACHE_TTL_MS) {
                logger.info("[Optimization] Search cache HIT for keyword: '{}'", keyword);
                return cached.results;
            } else {
                logger.info("[Optimization] Search cache EXPIRED for keyword: '{}'", keyword);
                searchCache.remove(keyword.toLowerCase());
            }
        }
        
        return null;
    }

    /**
     * Cache search results.
     */
    public void cacheResults(String keyword, List<PostDTO> results) {
        logger.info("[Optimization] Caching search results for keyword: '{}'", keyword);
        searchCache.put(keyword.toLowerCase(), new CachedSearchResult(results, System.currentTimeMillis()));
    }

    /**
     * Clear the search cache.
     */
    public void clearCache() {
        searchCache.clear();
    }

    private static class CachedSearchResult {
        final List<PostDTO> results;
        final long timestamp;

        CachedSearchResult(List<PostDTO> results, long timestamp) {
            this.results = results;
            this.timestamp = timestamp;
        }
    }
}
