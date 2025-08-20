package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.greenkitchen.portal.entities.PostCategory;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    PostCategory findBySlug(String slug);
    boolean existsBySlug(String slug);
}
