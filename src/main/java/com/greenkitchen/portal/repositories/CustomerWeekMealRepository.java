package com.greenkitchen.portal.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.CustomerWeekMeal;
import com.greenkitchen.portal.enums.MenuMealType;

@Repository
public interface CustomerWeekMealRepository extends JpaRepository<CustomerWeekMeal, Long> {

    @Query("SELECT cwm FROM CustomerWeekMeal cwm WHERE cwm.customer.id = :customerId AND cwm.type = :type AND cwm.weekStart = :weekStart AND cwm.isDeleted = false")
    Optional<CustomerWeekMeal> findByCustomerIdAndTypeAndWeekStart(
        @Param("customerId") Long customerId,
        @Param("type") MenuMealType type,
        @Param("weekStart") LocalDate weekStart);

    @Query("SELECT cwm FROM CustomerWeekMeal cwm WHERE cwm.customer.id = :customerId AND cwm.type = :type AND cwm.isDeleted = false ORDER BY cwm.weekStart DESC")
    List<CustomerWeekMeal> findByCustomerIdAndType(
        @Param("customerId") Long customerId,
        @Param("type") MenuMealType type);

    @Query("SELECT cwm FROM CustomerWeekMeal cwm WHERE cwm.customer.id = :customerId AND cwm.isDeleted = false ORDER BY cwm.weekStart DESC")
    List<CustomerWeekMeal> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT cwm FROM CustomerWeekMeal cwm WHERE cwm.id = :id AND cwm.isDeleted = false")
    Optional<CustomerWeekMeal> findById(@Param("id") Long id);
}
