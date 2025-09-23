package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.greenkitchen.portal.enums.PostStatus;

import com.greenkitchen.portal.entities.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE COALESCE(p.isDeleted, false) = false")
    List<Post> findAllActive();

    @Query("SELECT p FROM Post p WHERE COALESCE(p.isDeleted, false) = false ORDER BY p.createdAt DESC")
    Page<Post> findAllActivePaged(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE COALESCE(p.isDeleted, false) = false " +
        "AND (:status IS NULL OR p.status = :status) " +
        "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
        "AND (:q IS NULL OR p.title LIKE CONCAT('%', :q, '%') OR p.content LIKE CONCAT('%', :q, '%')) " +
        "ORDER BY p.createdAt DESC")
    Page<Post> findFilteredPaged(@Param("status") PostStatus status,
                 @Param("categoryId") Long categoryId,
                 @Param("q") String q,
                 Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Post p WHERE p.slug = :slug AND p.isDeleted = false")
    boolean existsBySlug(@Param("slug") String slug);

    Post findBySlug(String slug);
}
