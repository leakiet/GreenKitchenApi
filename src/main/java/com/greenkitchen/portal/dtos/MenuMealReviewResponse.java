package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuMealReviewResponse {
  private Long id;
  private Long menuMealId;
  private String menuMealTitle;
  private Long customerId;
  private String customerName;
  private Integer rating;
  private String comment;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
