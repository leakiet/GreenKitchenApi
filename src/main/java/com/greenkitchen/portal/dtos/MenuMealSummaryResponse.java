package com.greenkitchen.portal.dtos;

import java.util.Set;
import com.greenkitchen.portal.enums.MenuIngredients;
import com.greenkitchen.portal.enums.MenuMealType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuMealSummaryResponse {
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
    private int stock;
    private Integer soldCount;
    private MenuMealType type;
    private Set<MenuIngredients> menuIngredients;
    // Không có field reviews để tránh circular reference
}
