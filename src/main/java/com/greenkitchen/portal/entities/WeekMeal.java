package com.greenkitchen.portal.entities;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.greenkitchen.portal.enums.MenuMealType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "week_meals")
@Getter
@Setter
@NoArgsConstructor
public class WeekMeal extends AbstractEntity {
  private static final long serialVersionUID = 1L;

  private LocalDate weekStart;
  private LocalDate weekEnd;

  @Enumerated(EnumType.STRING)
  private MenuMealType type;

  @OneToMany(mappedBy = "weekMeal", cascade = CascadeType.ALL, fetch = FetchType.LAZY )
  @JsonManagedReference
  private List<WeekMealDay> days;
}
