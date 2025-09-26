package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.WeekMealDayResponse;
import com.greenkitchen.portal.dtos.WeekMealDayUpdateRequest;
import com.greenkitchen.portal.dtos.WeekMealRequest;
import com.greenkitchen.portal.dtos.WeekMealResponse;
import com.greenkitchen.portal.entities.WeekMealDay;

public interface WeekMealService {
  WeekMealResponse createWeekMeal(WeekMealRequest request);

  WeekMealResponse getWeekMealByTypeAndDate(String type, String date);

  WeekMealResponse updateWeekMeal(WeekMealRequest request);

  WeekMealResponse getWeekMealById(Long id);

  void deleteWeekMeal(Long id);

  WeekMealDay updateWeekMealDay(Long weekMealId, Long dayId, WeekMealDayUpdateRequest request);

  WeekMealDayResponse getWeekMealDayById(Long weekMealId, Long dayId);

}
