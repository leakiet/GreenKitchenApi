package com.greenkitchen.portal.dtos;

import java.util.List;
import java.util.Set;

import com.greenkitchen.portal.enums.Allergen;
import com.greenkitchen.portal.enums.MenuMealType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuMealResponse {
  private Long id;
  private String title;
  private String description;
  private Double calories;
  private Double protein;
  private Double carbs;
  private Double fat;
  private String image;
  private Double price;
  private String slug;
  private MenuMealType type;
  private Set<Allergen> allergens;
  private List<MenuMealReviewResponse> reviews;
}
