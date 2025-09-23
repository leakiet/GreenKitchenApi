package com.greenkitchen.portal.dtos;

import com.greenkitchen.portal.enums.OrderItemType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private OrderItemType itemType;
    private Long menuMealId;    // Null nếu là CustomMeal hoặc WeekMeal
    private Long customMealId;  // Null nếu là MenuMeal hoặc WeekMeal
    private Long weekMealId;   // Null nếu là MenuMeal hoặc CustomMeal
    private String weekMealTitle;
    private String weekMealDescription;
    private String weekMealImage;
    private Integer quantity;
    private Double unitPrice;
    private String notes;
}
