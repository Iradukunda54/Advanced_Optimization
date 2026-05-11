package com.blog.service;

import com.blog.dto.PostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PostService {
    PostDTO createPost(PostDTO postDTO);
    PostDTO getPostById(Long id);
    Page<PostDTO> getAllPosts(Pageable pageable);
    Page<PostDTO> getPostsByAuthor(String username, Pageable pageable);
    List<PostDTO> getPopularPosts(int limit);
    List<PostDTO> searchPosts(String keyword);
    PostDTO updatePost(Long id, PostDTO postDTO);
    void deletePost(Long id);
}
