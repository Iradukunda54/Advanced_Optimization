package com.blog.repository;

import com.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorId(Long authorId);
    
    Page<Post> findByAuthorUsername(String username, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Post> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p FROM Post p ORDER BY p.views DESC")
    List<Post> findTopPopularPosts(Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p JOIN post_tags pt ON p.id = pt.post_id JOIN tags t ON pt.tag_id = t.id WHERE t.name = :tagName", nativeQuery = true)
    List<Post> findByTagNameNative(@Param("tagName") String tagName);
}
