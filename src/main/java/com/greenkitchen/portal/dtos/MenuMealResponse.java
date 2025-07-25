package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.greenkitchen.portal.enums.MenuMealType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuMealResponse {
  private Long id;
  private String title;
  private String description;
  private Double calories;
  private Double protein;
  private Double carbs;
  private Double fat;
  private MenuMealType type;
  private String image;
  private Double price;
  private String slug;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Thông tin tổng hợp từ reviews
  private Double averageRating;
  private Integer totalReviews;
  private List<MenuMealReviewResponse> reviews;
}
