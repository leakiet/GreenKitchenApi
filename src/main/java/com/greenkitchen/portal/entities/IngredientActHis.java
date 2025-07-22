package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;

import com.greenkitchen.portal.enums.IngredientActionType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingredient_action_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngredientActHis extends AbstractEntity {
  @ManyToOne
  @JoinColumn(name = "customer_id")
  private Customer customer;
  @ManyToOne
  @JoinColumn(name = "ingredient_id")
  private Ingredients ingredient;
  @Enumerated(EnumType.STRING)
  private IngredientActionType actionType;
  private LocalDateTime timestamp = LocalDateTime.now();
}
