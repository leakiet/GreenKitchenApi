package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.CustomerWeekMealDayResponse;
import com.greenkitchen.portal.dtos.CustomerWeekMealDayUpdateRequest;
import com.greenkitchen.portal.dtos.CustomerWeekMealRequest;
import com.greenkitchen.portal.dtos.CustomerWeekMealResponse;
import com.greenkitchen.portal.entities.CustomerWeekMeal;
import com.greenkitchen.portal.entities.CustomerWeekMealDay;

public interface CustomerWeekMealService {
    CustomerWeekMeal createCustomerWeekMeal(CustomerWeekMealRequest request);

    CustomerWeekMealResponse getCustomerWeekMealByCustomerIdAndTypeAndDate(Long customerId, String type, String date);

    CustomerWeekMealResponse updateCustomerWeekMeal(CustomerWeekMealRequest request);

    CustomerWeekMealResponse getCustomerWeekMealById(Long id);

    void deleteCustomerWeekMeal(Long id);

    CustomerWeekMealDay updateCustomerWeekMealDay(Long customerWeekMealId, Long dayId, CustomerWeekMealDayUpdateRequest request);

    CustomerWeekMealDayResponse getCustomerWeekMealDayById(Long customerWeekMealId, Long dayId);

    java.util.List<CustomerWeekMealResponse> getCustomerWeekMealsByCustomerId(Long customerId);

    java.util.List<CustomerWeekMealResponse> getCustomerWeekMealsByCustomerIdAndType(Long customerId, String type);
}
