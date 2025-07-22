package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.entities.IngredientActHis;

public interface IngredientActHisService {
    List<IngredientActHis> findAll();
    IngredientActHis save(IngredientActHis ingredientActionHistory);
    List<IngredientActHis> findByCustomerId(Long CustomerId);
}
