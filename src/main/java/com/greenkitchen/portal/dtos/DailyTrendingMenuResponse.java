package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DailyTrendingMenuResponse {
  private Long id;
  private String title;
  private Double price;
  private Integer stock;
  private String type;
  private Long count;
  private String image;
  private String slug;
}