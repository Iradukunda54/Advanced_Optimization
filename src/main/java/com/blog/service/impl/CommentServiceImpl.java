package com.blog.service.impl;

import com.blog.dto.CommentDTO;
import com.blog.model.Comment;
import com.blog.model.Post;
import com.blog.model.User;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;
import com.blog.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CommentDTO addComment(CommentDTO commentDTO) {
        Post post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User author = userRepository.findById(commentDTO.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));
        
        Comment comment = new Comment(commentDTO.getContent(), post, author);
        Comment savedComment = commentRepository.save(comment);
        return mapToDTO(savedComment);
    }

    @Override
    public List<CommentDTO> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<CommentDTO> getCommentsByPost(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable).map(this::mapToDTO);
    }

    private CommentDTO mapToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setPostId(comment.getPost().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getUsername());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
