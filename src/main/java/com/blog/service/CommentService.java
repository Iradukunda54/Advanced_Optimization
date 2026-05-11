package com.blog.service;

import com.blog.dto.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CommentService {
    CommentDTO addComment(CommentDTO commentDTO);
    List<CommentDTO> getCommentsByPost(Long postId);
    Page<CommentDTO> getCommentsByPost(Long postId, Pageable pageable);
}
