package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.greenkitchen.portal.entities.CustomMeal;

public interface CustomMealRepository extends JpaRepository<CustomMeal, Long> {
  List<CustomMeal> findAllByCustomerId(Long customerId);
}
