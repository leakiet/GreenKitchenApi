package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.dtos.MenuMealReviewRequest;
import com.greenkitchen.portal.entities.MenuMealReview;

public interface MenuMealReviewService {
    MenuMealReview getMenuMealReviewById(Long id);
    MenuMealReview createMenuMealReview(MenuMealReviewRequest dto);
    MenuMealReview updateMenuMealReview(Long id, MenuMealReviewRequest dto);
    void deleteMenuMealReview(Long id);
    List<MenuMealReview> getAllReviewsByMenuMealId(Long menuMealId);
    List<MenuMealReview> getAllReviewsByCustomerId(Long customerId);
}