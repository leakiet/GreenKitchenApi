package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.dtos.RequestMenuMeal;
import com.greenkitchen.portal.entities.MenuMeal;

public interface MenuMealService {
  MenuMeal createMenuMeal(RequestMenuMeal dto);
  MenuMeal getMenuMealById(Long id);
  MenuMeal getMenuMealBySlug(String slug);
  List<MenuMeal> getAllMenuMeals();
  MenuMeal updateMenuMeal(Long id, RequestMenuMeal dto);
  void deleteMenuMeal(Long id);
}
