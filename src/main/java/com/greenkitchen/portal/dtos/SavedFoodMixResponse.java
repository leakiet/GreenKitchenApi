package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedFoodMixResponse {
    private Long id;
    private Long customerId;
    private List<IngredientQuantityResponse> proteins;
    private List<IngredientQuantityResponse> carbs;
    private List<IngredientQuantityResponse> sides;
    private List<IngredientQuantityResponse> sauces;
    private String note;
}
