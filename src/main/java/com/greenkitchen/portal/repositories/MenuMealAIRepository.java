package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.MenuMeal;

public interface MenuMealAIRepository  extends JpaRepository<MenuMeal, Long> {
	
	@Query("SELECT m FROM MenuMeal m WHERE " +
		       "LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		       "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
		List<MenuMeal> aiSearchByKeyword(@Param("keyword") String keyword, Pageable pageable);

	
}
	