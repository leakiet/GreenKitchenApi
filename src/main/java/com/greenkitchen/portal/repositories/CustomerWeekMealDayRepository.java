package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.CustomerWeekMealDay;

@Repository
public interface CustomerWeekMealDayRepository extends JpaRepository<CustomerWeekMealDay, Long> {

    @Query("SELECT cwmd FROM CustomerWeekMealDay cwmd WHERE cwmd.id = :id AND cwmd.isDeleted = false")
    java.util.Optional<CustomerWeekMealDay> findById(@Param("id") Long id);
}
