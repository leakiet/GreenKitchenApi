package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.IngredientActHis;

public interface IngredientActHisRepository extends JpaRepository<IngredientActHis, Long> {
    @Query("select i from IngredientActHis i where i.customer.id = :customerId")
    List<IngredientActHis> findAllByCustomerId(@Param("customerId") Long customerId);
}
