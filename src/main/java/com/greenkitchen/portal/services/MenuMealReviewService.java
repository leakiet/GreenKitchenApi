package com.greenkitchen.portal.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.greenkitchen.portal.dtos.MenuMealReviewRequest;
import com.greenkitchen.portal.dtos.MenuMealReviewResponse;
import com.greenkitchen.portal.dtos.PagedResponse;
import com.greenkitchen.portal.entities.MenuMealReview;

public interface MenuMealReviewService {
    MenuMealReview getMenuMealReviewById(Long id);
    MenuMealReviewResponse createMenuMealReview(MenuMealReviewRequest dto);
    MenuMealReviewResponse updateMenuMealReview(Long id, MenuMealReviewRequest dto);
    void deleteMenuMealReview(Long id);
    List<MenuMealReviewResponse> getAllReviewsByMenuMealId(Long menuMealId);
    List<MenuMealReviewResponse> getAllReviewsByCustomerId(Long customerId);
    PagedResponse<MenuMealReviewResponse> listFilteredPaged(int page, int size, String status, String q);
    List<MenuMealReviewResponse> listAll();
    PagedResponse<MenuMealReviewResponse> getPagedReviewsByMenuMealId(Long menuMealId, int page, int size, String status, String q);
}