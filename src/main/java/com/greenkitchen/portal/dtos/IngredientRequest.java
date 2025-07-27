package com.greenkitchen.portal.dtos;

import com.greenkitchen.portal.enums.IngredientType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngredientRequest {
    private String title;
    @Enumerated(EnumType.STRING)
    private IngredientType type;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String description;
    private String image;
}
