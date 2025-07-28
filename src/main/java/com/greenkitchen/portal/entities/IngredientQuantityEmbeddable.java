package com.greenkitchen.portal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngredientQuantityEmbeddable {
    @Column(name = "ingredient_id")
    private Long ingredientId;
    
    @Column(name = "quantity")
    private Integer quantity;
}
