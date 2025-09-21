package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.Ingredients;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredients, Long> {
    List<Ingredients> findAll();
    boolean existsByTitle(String title);
}