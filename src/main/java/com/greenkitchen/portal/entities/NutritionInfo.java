package com.greenkitchen.portal.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NutritionInfo {
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
}
