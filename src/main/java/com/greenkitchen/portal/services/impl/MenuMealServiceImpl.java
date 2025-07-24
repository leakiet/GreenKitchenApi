package com.greenkitchen.portal.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.RequestMenuMeal;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.services.MenuMealService;

@Service
public class MenuMealServiceImpl implements MenuMealService {
    @Override
    public MenuMeal createMenuMeal(RequestMenuMeal dto) {
        return null; // Placeholder return
    }

    @Override
    public MenuMeal getMenuMealById(Long id) {
        return null; // Placeholder return
    }

    @Override
    public MenuMeal getMenuMealBySlug(String slug) {
        // Logic to retrieve a MenuMeal by slug
        return null; // Placeholder return
    }

    @Override
    public List<MenuMeal> getAllMenuMeals() {
        // Logic to retrieve all MenuMeals
        return null; // Placeholder return
    }

    @Override
    public MenuMeal updateMenuMeal(Long id, RequestMenuMeal dto) {
        // Logic to update an existing MenuMeal
        return null; // Placeholder return
    }

    @Override
    public void deleteMenuMeal(Long id) {
        // Logic to delete a MenuMeal by ID
    }
  
}
