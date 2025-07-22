package com.greenkitchen.portal.entities;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "saved_food_mix")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SavedFoodMix extends AbstractEntity{
  @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ElementCollection
    @CollectionTable(name = "saved_food_mix_proteins", joinColumns = @JoinColumn(name = "food_mix_id"))
    private List<IngredientQuantityEmbeddable> proteins;

    @ElementCollection
    @CollectionTable(name = "saved_food_mix_carbs", joinColumns = @JoinColumn(name = "food_mix_id"))
    private List<IngredientQuantityEmbeddable> carbs;

    @ElementCollection
    @CollectionTable(name = "saved_food_mix_sides", joinColumns = @JoinColumn(name = "food_mix_id"))
    private List<IngredientQuantityEmbeddable> sides;

    @ElementCollection
    @CollectionTable(name = "saved_food_mix_sauces", joinColumns = @JoinColumn(name = "food_mix_id"))
    private List<IngredientQuantityEmbeddable> sauces;
    private List<Long> sauceIds;

    @Column(name = "note")
    private String note;

}
