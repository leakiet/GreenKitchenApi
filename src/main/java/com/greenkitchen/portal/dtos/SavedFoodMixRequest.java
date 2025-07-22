package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedFoodMixRequest {
    private Long customerId;
    private List<IngredientQuantity> proteins;
    private List<IngredientQuantity> carbs;
    private List<IngredientQuantity> sides;
    private List<IngredientQuantity> sauces;
    private String note;
}
