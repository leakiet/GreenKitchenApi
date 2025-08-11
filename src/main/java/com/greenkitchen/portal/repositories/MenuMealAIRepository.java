package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.MenuMeal;

public interface MenuMealAIRepository  extends JpaRepository<MenuMeal, Long> {
	
    @Query("SELECT m FROM MenuMeal m WHERE m.isDeleted = false")
    List<MenuMeal> findAllActive();
	
}
	