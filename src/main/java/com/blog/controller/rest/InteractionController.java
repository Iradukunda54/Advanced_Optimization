package com.blog.controller.rest;

import com.blog.dto.ApiResponse;
import com.blog.dto.CommentDTO;
import com.blog.dto.ReviewDTO;
import com.blog.service.CommentService;
import com.blog.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InteractionController {

    private final CommentService commentService;
    private final ReviewService reviewService;

    public InteractionController(CommentService commentService, ReviewService reviewService) {
        this.commentService = commentService;
        this.reviewService = reviewService;
    }

    @PostMapping("/comments")
    public ApiResponse<CommentDTO> addComment(@Valid @RequestBody CommentDTO commentDTO) {
        return ApiResponse.success("Comment added successfully", commentService.addComment(commentDTO));
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<Page<CommentDTO>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Comments retrieved successfully", commentService.getCommentsByPost(postId, pageable));
    }

    @PostMapping("/reviews")
    public ApiResponse<ReviewDTO> addReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        return ApiResponse.success("Review added successfully", reviewService.addReview(reviewDTO));
    }

    @GetMapping("/posts/{postId}/reviews")
    public ApiResponse<Page<ReviewDTO>> getReviews(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Reviews retrieved successfully", reviewService.getReviewsByPost(postId, pageable));
    }
}
