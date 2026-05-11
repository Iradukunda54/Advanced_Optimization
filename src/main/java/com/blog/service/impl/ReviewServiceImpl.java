package com.blog.service.impl;

import com.blog.dto.ReviewDTO;
import com.blog.model.Post;
import com.blog.model.Review;
import com.blog.model.User;
import com.blog.repository.PostRepository;
import com.blog.repository.ReviewRepository;
import com.blog.repository.UserRepository;
import com.blog.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, PostRepository postRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReviewDTO addReview(ReviewDTO reviewDTO) {
        Post post = postRepository.findById(reviewDTO.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User author = userRepository.findById(reviewDTO.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));
        
        Review review = new Review(reviewDTO.getRating(), reviewDTO.getContent(), post, author);
        Review savedReview = reviewRepository.save(review);
        return mapToDTO(savedReview);
    }

    @Override
    public List<ReviewDTO> getReviewsByPost(Long postId) {
        return reviewRepository.findByPostId(postId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<ReviewDTO> getReviewsByPost(Long postId, Pageable pageable) {
        return reviewRepository.findByPostId(postId, pageable).map(this::mapToDTO);
    }

    private ReviewDTO mapToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setPostId(review.getPost().getId());
        dto.setAuthorId(review.getAuthor().getId());
        dto.setAuthorName(review.getAuthor().getUsername());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
