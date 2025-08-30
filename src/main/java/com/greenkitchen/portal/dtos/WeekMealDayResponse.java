package com.greenkitchen.portal.dtos;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeekMealDayResponse {
  private Long id;
  private String day;
  private LocalDate date;
  private String type;
  private MenuMealResponse meal1; // Trả về luôn entity hoặc DTO của MenuMeal
  private MenuMealResponse meal2;
  private MenuMealResponse meal3;
}
