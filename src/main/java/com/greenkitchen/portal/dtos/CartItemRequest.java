package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
  private Long cartId;
  private Boolean isCustom;
  private Long menuMealId; // For menu items
  private Long customMealId; // For custom items
  private Integer quantity;
  private Double unitPrice;
  private Double totalPrice;
  private String title;
  private String description;
  private String image; 
  private String itemType;
  
  // Nutrition info
  private Double calories;
  private Double protein;
  private Double carbs;
  private Double fat;
}
