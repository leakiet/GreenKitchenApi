package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.dtos.MenuMealRequest;
import com.greenkitchen.portal.entities.MenuMeal;

public interface MenuMealService {
  MenuMeal createMenuMeal(MenuMealRequest dto);
  MenuMeal getMenuMealById(Long id);
  MenuMeal getMenuMealBySlug(String slug);
  List<MenuMeal> getAllMenuMeals();
  MenuMeal updateMenuMeal(Long id, MenuMealRequest dto);
  void deleteMenuMeal(Long id);
  boolean existsBySlug(String slug);
}
