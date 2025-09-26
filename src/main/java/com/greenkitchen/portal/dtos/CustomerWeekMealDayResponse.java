package com.greenkitchen.portal.dtos;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerWeekMealDayResponse {
    private Long id;
    private String day;
    private LocalDate date;
    private String type;
  private MenuMealSummaryResponse meal1;
  private MenuMealSummaryResponse meal2;
  private MenuMealSummaryResponse meal3;
}
