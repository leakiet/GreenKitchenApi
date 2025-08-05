package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.dtos.CustomMealRequest;
import com.greenkitchen.portal.dtos.CustomMealResponse;

public interface CustomMealService {
    List<CustomMealResponse> getAllCustomMeals();

    List<CustomMealResponse> getCustomMealsByCustomerId(Long customerId);

    CustomMealResponse createCustomMeal(CustomMealRequest request);

    CustomMealResponse updateCustomMeal(Long id, CustomMealRequest request);

    void deleteCustomMeal(Long id);

    CustomMealResponse findById(Long id);
}
