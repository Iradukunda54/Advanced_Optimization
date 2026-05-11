package com.blog.repository;

import com.blog.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPostId(Long postId);
    Page<Review> findByPostId(Long postId, Pageable pageable);
}
