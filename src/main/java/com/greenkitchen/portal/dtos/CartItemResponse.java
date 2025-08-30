package com.greenkitchen.portal.dtos;

import java.util.List;

import com.greenkitchen.portal.enums.MenuMealType;
import com.greenkitchen.portal.enums.OrderItemType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long cartId;
    private Boolean isCustom;
    
    private MenuMealResponse menuMeal;
    private CustomMealResponse customMeal;
    private WeekMealResponse weekMeal;
    private OrderItemType itemType;
    
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String title;
    private String image;
    private String description;
    
    // Nutrition info
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
}
