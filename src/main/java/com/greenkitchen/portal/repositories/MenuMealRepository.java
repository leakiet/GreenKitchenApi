package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.MenuMeal;

public interface MenuMealRepository extends JpaRepository<MenuMeal, Long> {
    // Define methods for CRUD operations on MenuMeal entities
    // For example:
    // List<MenuMeal> findByType(MenuMealType type);
    // Optional<MenuMeal> findById(Long id);
    // void deleteById(Long id);
    List<MenuMeal> findAll();

    @Query("SELECT m FROM MenuMeal m WHERE m.isDeleted = false")
    List<MenuMeal> findAllActive();

    @Query("SELECT m FROM MenuMeal m WHERE m.slug = :slug AND m.isDeleted = false")
    MenuMeal findBySlugActive(@Param("slug") String slug);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MenuMeal m WHERE m.slug = :slug AND m.isDeleted = false")
    boolean existsBySlug(@Param("slug") String slug);

    @Query("SELECT m FROM MenuMeal m WHERE m.isDeleted = false ORDER BY m.soldCount DESC")
    List<MenuMeal> findTop10BySoldCount();

}
