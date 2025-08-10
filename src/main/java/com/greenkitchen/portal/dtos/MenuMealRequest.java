package com.greenkitchen.portal.dtos;

import java.util.Set;

import com.greenkitchen.portal.enums.Allergen;
import com.greenkitchen.portal.enums.MenuMealType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuMealRequest {
    private String title;
    private String description;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    @Enumerated(EnumType.STRING)
    private MenuMealType type;
    private String image;
    private Double price;
    private String slug;
    private Set<Allergen> allergens;
    private String allergensString;
}
