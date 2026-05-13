package com.blog.service.impl;

import com.blog.dto.PostDTO;
import com.blog.model.Post;
import com.blog.model.Tag;
import com.blog.model.User;
import com.blog.repository.PostRepository;
import com.blog.repository.TagRepository;
import com.blog.repository.UserRepository;
import com.blog.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final NotificationService notificationService;
    private final ConcurrentViewCounter viewCounter;
    private final TrendingPostCache trendingCache;
    private final SearchOptimizationService searchOptimizationService;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, 
                           TagRepository tagRepository, NotificationService notificationService,
                           ConcurrentViewCounter viewCounter, TrendingPostCache trendingCache,
                           SearchOptimizationService searchOptimizationService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.notificationService = notificationService;
        this.viewCounter = viewCounter;
        this.trendingCache = trendingCache;
        this.searchOptimizationService = searchOptimizationService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public PostDTO createPost(PostDTO postDTO) {
        User author = userRepository.findById(postDTO.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));
        Post post = new Post(postDTO.getTitle(), postDTO.getContent(), author);
        
        if (postDTO.getTags() != null) {
            Set<Tag> tags = postDTO.getTags().stream()
                    .map(name -> tagRepository.findByName(name)
                            .orElseGet(() -> tagRepository.save(new Tag(name))))
                    .collect(Collectors.toSet());
            post.setTags(tags);
        }
        
        Post savedPost = postRepository.save(post);
        
        // ASYNC: Dispatch notification without blocking the main thread
        notificationService.notifyOnNewPost(savedPost);
        
        return mapToDTO(savedPost);
    }

    @Override
    @Cacheable(value = "posts", key = "#id")
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        
        // CONCURRENCY: Use thread-safe view counter instead of direct DB update
        viewCounter.incrementView(id);
        
        return mapToDTO(post);
    }

    @Override
    public Page<PostDTO> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public Page<PostDTO> getPostsByAuthor(String username, Pageable pageable) {
        return postRepository.findByAuthorUsername(username, pageable).map(this::mapToDTO);
    }

    @Override
    public List<PostDTO> getPopularPosts(int limit) {
        // DSA: Use trending cache (PriorityQueue based) for fast retrieval
        List<PostDTO> trending = trendingCache.getTrendingPosts(limit);
        if (!trending.isEmpty()) {
            return trending;
        }
        
        // Fallback to DB if cache is empty
        return postRepository.findTopPopularPosts(org.springframework.data.domain.PageRequest.of(0, limit))
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> searchPosts(String keyword) {
        // ALGORITHMIC OPTIMIZATION: Check search cache first
        List<PostDTO> cached = searchOptimizationService.getCachedResults(keyword);
        if (cached != null) return cached;
        
        List<PostDTO> results = postRepository.searchByKeyword(keyword).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        searchOptimizationService.cacheResults(keyword, results);
        return results;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "popularPosts", "searchResults"}, allEntries = true)
    public PostDTO updatePost(Long id, PostDTO postDTO) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        Post updatedPost = postRepository.save(post);
        return mapToDTO(updatedPost);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "popularPosts", "searchResults"}, allEntries = true)
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    private PostDTO mapToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorName(post.getAuthor().getUsername());
        dto.setCreatedAt(post.getCreatedAt());
        
        // Combine DB views with current in-memory view buffer
        int currentViews = post.getViews() + viewCounter.getViewCount(post.getId());
        dto.setViews(currentViews);
        
        if (post.getTags() != null) {
            dto.setTags(post.getTags().stream().map(Tag::getName).collect(Collectors.toSet()));
        }
        return dto;
    }
}
