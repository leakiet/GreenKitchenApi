package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.WeekMealDay;

@Repository
public interface WeekMealDayRepository extends JpaRepository<WeekMealDay, Long> {
    // Có thể thêm query nếu cần, e.g., findByWeekMealIdAndDay
}
