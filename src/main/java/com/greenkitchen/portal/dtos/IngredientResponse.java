package com.greenkitchen.portal.dtos;

import com.greenkitchen.portal.entities.NutritionInfo;
import com.greenkitchen.portal.enums.IngredientType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngredientResponse {
    private Long id;
    private String title;
    private IngredientType type;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String description;
    private String image;
    private Double price;
    private Integer stock;
}
