package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.dtos.MenuMealReviewRequest;
import com.greenkitchen.portal.dtos.MenuMealReviewResponse;
import com.greenkitchen.portal.entities.MenuMealReview;

public interface MenuMealReviewService {
    MenuMealReview getMenuMealReviewById(Long id);
    MenuMealReviewResponse createMenuMealReview(MenuMealReviewRequest dto);
    MenuMealReviewResponse updateMenuMealReview(Long id, MenuMealReviewRequest dto);
    void deleteMenuMealReview(Long id);
    List<MenuMealReviewResponse> getAllReviewsByMenuMealId(Long menuMealId);
    List<MenuMealReviewResponse> getAllReviewsByCustomerId(Long customerId);
}