package com.blog.service;

import com.blog.dto.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ReviewService {
    ReviewDTO addReview(ReviewDTO reviewDTO);
    List<ReviewDTO> getReviewsByPost(Long postId);
    Page<ReviewDTO> getReviewsByPost(Long postId, Pageable pageable);
}
