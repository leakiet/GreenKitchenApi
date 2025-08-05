package com.greenkitchen.portal.dtos;

import java.util.List;

import com.greenkitchen.portal.enums.MenuMealType;

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
    
    // Menu meal data (khi isCustom = false)
    private Long menuMealId;
    private String menuMealTitle;
    private String menuMealDescription;
    private String menuMealImage;
    private Double menuMealPrice;
    private String menuMealSlug;
    private String menuMealType;
    
    // Custom meal data (khi isCustom = true)
    private Long customMealId;
    private String customMealName;
    private List<CustomMealDetailResponse> details;
    
    private Integer quantity;
    private Double basePrice;
    private Double totalPrice;
    private String title;
    private String description;
    
    // Nutrition info
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
}
