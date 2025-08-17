package com.greenkitchen.portal.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.WeekMeal;
import com.greenkitchen.portal.enums.MenuMealType;

@Repository
public interface WeekMealRepository extends JpaRepository<WeekMeal, Long> {
  // Optional<WeekMeal> findByTypeAndWeekStart(MenuMealType type, LocalDate
  // weekStart);

  @Query("SELECT w FROM WeekMeal w WHERE w.type = :type AND w.weekStart = :weekStart AND w.isDeleted = false")
  Optional<WeekMeal> findByTypeAndWeekStart(
      @Param("type") MenuMealType type,
      @Param("weekStart") LocalDate weekStart);

  @Query("SELECT w FROM WeekMeal w WHERE w.id = :id AND w.isDeleted = false")
  Optional<WeekMeal> findById(@Param("id") Long id);
}
