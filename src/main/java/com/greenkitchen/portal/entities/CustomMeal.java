package com.greenkitchen.portal.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "custom_meals")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomMeal extends AbstractEntity {
  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(nullable = false)
  private String name;

  @Embedded
  private NutritionInfo nutrition;

  private Double price;
  private String description;
  private String image;

  // @JsonIgnore
  @OneToMany(mappedBy = "customMeal", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CustomMealDetail> details;
}
