package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.WeekMealRequest;
import com.greenkitchen.portal.dtos.WeekMealResponse;
import com.greenkitchen.portal.entities.WeekMeal;

public interface WeekMealService {
  WeekMeal createWeekMeal(WeekMealRequest request);

  WeekMealResponse getWeekMealByTypeAndDate(String type, String date);

  WeekMealResponse updateWeekMeal(WeekMealRequest request);

  WeekMealResponse getWeekMealById(Long id);

  void deleteWeekMeal(Long id);
}
