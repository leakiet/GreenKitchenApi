package com.greenkitchen.portal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "custom_meal_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomMealDetail extends AbstractEntity{
  @ManyToOne
  @JoinColumn(name = "custom_meal_id", nullable = false)
  private CustomMeal customMeal;

  @Column(name = "ingredient_id", nullable = false)
  private Long ingredientId;

  private Double quantity;
}
