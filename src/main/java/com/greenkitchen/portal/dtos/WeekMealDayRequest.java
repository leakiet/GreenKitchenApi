package com.greenkitchen.portal.dtos;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeekMealDayRequest {
  private Long id;
  private String day;
  private LocalDate date;
  private Long meal1;
  private Long meal2;
}