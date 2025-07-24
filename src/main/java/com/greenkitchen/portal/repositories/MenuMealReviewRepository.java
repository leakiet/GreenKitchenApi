package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.greenkitchen.portal.entities.MenuMealReview;

public interface MenuMealReviewRepository extends JpaRepository<MenuMealReview, Long> {
    // Define methods for CRUD operations on MenuMeal entities
    // For example:
    // List<MenuMeal> findByType(MenuMealType type);
    // Optional<MenuMeal> findById(Long id);
    // void deleteById(Long id);
  
}
