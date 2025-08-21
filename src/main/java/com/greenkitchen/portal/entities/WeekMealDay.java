package com.greenkitchen.portal.entities;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "week_meal_days")
@Getter
@Setter
@NoArgsConstructor
public class WeekMealDay extends AbstractEntity {
  private static final long serialVersionUID = 1L;

  private String day;
  private LocalDate date;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "week_meal_id")
  @JsonBackReference
  private WeekMeal weekMeal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meal1_id")
  private MenuMeal meal1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meal2_id")
  private MenuMeal meal2;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meal3_id")
  private MenuMeal meal3;
}
