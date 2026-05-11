package com.blog.controller.rest;

import com.blog.dto.ApiResponse;
import com.blog.dto.PostDTO;
import com.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {

        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public ApiResponse<PostDTO> createPost(@Valid @RequestBody PostDTO postDTO) {
        return ApiResponse.success("Post created successfully", postService.createPost(postDTO));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDTO> getPost(@PathVariable Long id) {
        return ApiResponse.success("Post retrieved successfully", postService.getPostById(id));
    }

    @GetMapping
    public ApiResponse<Page<PostDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        
        String sortField = sort[0];
        String sortDir = sort.length > 1 ? sort[1] : "asc";
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return ApiResponse.success("Posts retrieved successfully", postService.getAllPosts(pageable));
    }

    @GetMapping("/search")
    public ApiResponse<List<PostDTO>> searchPosts(@RequestParam String keyword) {
        return ApiResponse.success("Search results retrieved successfully", postService.searchPosts(keyword));
    }

    @GetMapping("/popular")
    public ApiResponse<List<PostDTO>> getPopularPosts(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.success("Popular posts retrieved successfully", postService.getPopularPosts(limit));
    }

    @GetMapping("/author/{username}")
    public ApiResponse<Page<PostDTO>> getPostsByAuthor(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Author's posts retrieved successfully", postService.getPostsByAuthor(username, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public ApiResponse<PostDTO> updatePost(@PathVariable Long id, @Valid @RequestBody PostDTO postDTO) {
        return ApiResponse.success("Post updated successfully", postService.updatePost(id, postDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ApiResponse.success("Post deleted successfully", null);
    }
}
