package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.MenuMeal;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MenuMealRepository extends JpaRepository<MenuMeal, Long> {
    // Define methods for CRUD operations on MenuMeal entities
    // For example:
    // List<MenuMeal> findByType(MenuMealType type);
    // Optional<MenuMeal> findById(Long id);
    // void deleteById(Long id);
  
}
