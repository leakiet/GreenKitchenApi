package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.greenkitchen.portal.entities.CustomMealDetail;

public interface CustomMealDetailRepository extends JpaRepository<CustomMealDetail, Long> {
}
