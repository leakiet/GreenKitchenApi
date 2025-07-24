package com.greenkitchen.portal.services;


import com.greenkitchen.portal.dtos.RequestMenuMeal;
import com.greenkitchen.portal.entities.MenuMeal;


public interface MenuMealReviewService {
  MenuMeal getMenuMealById(Long id);
  MenuMeal createMenuMeal(RequestMenuMeal dto);
  MenuMeal updateMenuMeal(Long id, RequestMenuMeal dto);
  void deleteMenuMeal(Long id);
}
