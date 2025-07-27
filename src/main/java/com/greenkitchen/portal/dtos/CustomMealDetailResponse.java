package com.greenkitchen.portal.dtos;

import com.greenkitchen.portal.enums.IngredientType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomMealDetailResponse {
  private Long id;
  private String title;
  private IngredientType type;
  private Double calories;
  private Double protein;
  private Double carbs;
  private Double fat;
  private String description;
  private String image;
  private Double quantity;
}
