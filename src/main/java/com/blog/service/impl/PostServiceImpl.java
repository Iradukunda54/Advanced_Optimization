package com.blog.service.impl;

import com.blog.dto.PostDTO;
import com.blog.model.Post;
import com.blog.model.Tag;
import com.blog.model.User;
import com.blog.repository.PostRepository;
import com.blog.repository.TagRepository;
import com.blog.repository.UserRepository;
import com.blog.service.PostService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
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
        return mapToDTO(savedPost);
    }

    @Override
    @Cacheable(value = "posts", key = "#id")
    public PostDTO getPostById(Long id) {
        System.out.println("db");
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setViews(post.getViews() + 1); // Increment views
        postRepository.save(post);
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
    @Cacheable(value = "popularPosts")
    public List<PostDTO> getPopularPosts(int limit) {
        return postRepository.findTopPopularPosts(PageRequest.of(0, limit))
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> searchPosts(String keyword) {
        return postRepository.searchByKeyword(keyword).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "popularPosts"}, allEntries = true)
    public PostDTO updatePost(Long id, PostDTO postDTO) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        Post updatedPost = postRepository.save(post);
        return mapToDTO(updatedPost);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "popularPosts"}, allEntries = true)
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
        dto.setViews(post.getViews());
        if (post.getTags() != null) {
            dto.setTags(post.getTags().stream().map(Tag::getName).collect(Collectors.toSet()));
        }
        return dto;
    }
}
