package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false")
    List<Post> findAllActive();

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Post p WHERE p.slug = :slug AND p.isDeleted = false")
    boolean existsBySlug(@Param("slug") String slug);

    Post findBySlug(String slug);
}
