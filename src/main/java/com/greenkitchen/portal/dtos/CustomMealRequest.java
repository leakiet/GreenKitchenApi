package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomMealRequest {
  private Long customerId;
  private String title;
  private Double protein;
  private Double calories;
  private Double carb;
  private Double fat;
  private Double price;
  private String description;
  private String image;
  private List<CustomMealDetailRequest> proteins;
  private List<CustomMealDetailRequest> carbs;
  private List<CustomMealDetailRequest> sides;
  private List<CustomMealDetailRequest> sauces;
}
