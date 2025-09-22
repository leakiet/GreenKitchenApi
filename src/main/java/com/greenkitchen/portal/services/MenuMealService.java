package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.dtos.MenuMealRequest;
import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.entities.MenuMeal;

public interface MenuMealService {
    MenuMeal createMenuMeal(MenuMealRequest dto);
    MenuMealResponse getMenuMealById(Long id);
    MenuMealResponse getMenuMealBySlug(String slug);
    List<MenuMealResponse> getAllMenuMeals();
    MenuMeal updateMenuMeal(Long id, MenuMealRequest dto);
    void deleteMenuMeal(Long id);
    boolean existsBySlug(String slug);
    void incrementSoldCount(Long menuMealId);
    List<MenuMealResponse> getPopularMenuMeals();

}
