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
  private MenuMealSummaryResponse meal1; // Sử dụng summary response để tránh circular reference
  private MenuMealSummaryResponse meal2;
  private MenuMealSummaryResponse meal3;
}
