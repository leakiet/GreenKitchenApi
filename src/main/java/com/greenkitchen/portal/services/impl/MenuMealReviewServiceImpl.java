package com.greenkitchen.portal.services.impl;

import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.RequestMenuMeal;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.services.MenuMealReviewService;

@Service
public class MenuMealReviewServiceImpl implements MenuMealReviewService{

  @Override
  public MenuMeal getMenuMealById(Long id) {
    throw new UnsupportedOperationException("Unimplemented method 'getMenuMealById'");
  }

  @Override
  public MenuMeal createMenuMeal(RequestMenuMeal dto) {
    throw new UnsupportedOperationException("Unimplemented method 'createMenuMeal'");
  }

  @Override
  public MenuMeal updateMenuMeal(Long id, RequestMenuMeal dto) {
    throw new UnsupportedOperationException("Unimplemented method 'updateMenuMeal'");
  }

  @Override
  public void deleteMenuMeal(Long id) {
    throw new UnsupportedOperationException("Unimplemented method 'deleteMenuMeal'");
  }
    
}
