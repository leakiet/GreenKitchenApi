package com.greenkitchen.portal.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.MenuMealRequest;
import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.dtos.MenuMealReviewResponse;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.entities.NutritionInfo;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.services.MenuMealService;

@Service
public class MenuMealServiceImpl implements MenuMealService {

    @Autowired
    private MenuMealRepository menuMealRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public MenuMeal createMenuMeal(MenuMealRequest dto) {
        MenuMeal menuMeal = new MenuMeal();
        if (menuMeal.getNutrition() == null) {
            menuMeal.setNutrition(new NutritionInfo());
        }

        menuMeal.getNutrition().setCalories(dto.getCalories());
        menuMeal.getNutrition().setProtein(dto.getProtein());
        menuMeal.getNutrition().setCarbs(dto.getCarbs());
        menuMeal.getNutrition().setFat(dto.getFat());
        modelMapper.map(dto, menuMeal);

        return menuMealRepository.save(menuMeal);
    }

    @Override
    public MenuMealResponse getMenuMealById(Long id) {
        MenuMeal menuMeal = menuMealRepository.findById(id).orElse(null);
        return menuMeal != null ? toResponse(menuMeal) : null;
    }

    @Override
    public MenuMealResponse getMenuMealBySlug(String slug) {
        MenuMeal menuMeal = menuMealRepository.findBySlugActive(slug);
        return menuMeal != null ? toResponse(menuMeal) : null;
    }

    @Override
    public List<MenuMealResponse> getAllMenuMeals() {
        return menuMealRepository.findAllActive().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public MenuMeal updateMenuMeal(Long id, MenuMealRequest dto) {
        MenuMeal existingMenuMeal = menuMealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuMeal not found with id: " + id));

        modelMapper.map(dto, existingMenuMeal);
        existingMenuMeal.setId(id);

        return menuMealRepository.save(existingMenuMeal);
    }

    @Override
    public void deleteMenuMeal(Long id) {
        try {
            MenuMeal menuMeal = menuMealRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("MenuMeal not found with id: " + id));

            if (menuMeal.getIsDeleted()) {
                throw new RuntimeException("MenuMeal with id: " + id + " is already deleted");
            }

            menuMeal.setIsDeleted(true);
            menuMealRepository.save(menuMeal);

        } catch (RuntimeException e) {
            System.err.println("Error deleting MenuMeal: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred while deleting MenuMeal: " + e.getMessage());
            throw new RuntimeException("Failed to delete MenuMeal with id: " + id, e);
        }
    }

    @Override
    public boolean existsBySlug(String slug) {
        return menuMealRepository.existsBySlug(slug);
    }

    private MenuMealResponse toResponse(MenuMeal menuMeal) {
        MenuMealResponse response = new MenuMealResponse();

        response.setId(menuMeal.getId());
        response.setTitle(menuMeal.getTitle());
        response.setDescription(menuMeal.getDescription());
        response.setType(menuMeal.getType());
        response.setImage(menuMeal.getImage());
        response.setPrice(menuMeal.getPrice());
        response.setSlug(menuMeal.getSlug());

        if (menuMeal.getNutrition() != null) {
            response.setCalories(menuMeal.getNutrition().getCalories());
            response.setProtein(menuMeal.getNutrition().getProtein());
            response.setCarbs(menuMeal.getNutrition().getCarbs());
            response.setFat(menuMeal.getNutrition().getFat());
        }

        if (menuMeal.getReviews() != null) {
            List<MenuMealReviewResponse> reviewResponses = menuMeal.getReviews().stream()
                .map(review -> {
                    MenuMealReviewResponse reviewResponse = new MenuMealReviewResponse();
                    reviewResponse.setId(review.getId());
                    reviewResponse.setRating(review.getRating());
                    reviewResponse.setComment(review.getComment());
                    
                    reviewResponse.setMenuMealId(menuMeal.getId());
                    reviewResponse.setMenuMealTitle(menuMeal.getTitle());
                    
                    if (review.getCustomer() != null) {
                        reviewResponse.setCustomerId(review.getCustomer().getId());
                        reviewResponse.setCustomerName(
                            review.getCustomer().getFirstName() + " " + review.getCustomer().getLastName()
                        );
                    }
                    
                    reviewResponse.setCreatedAt(review.getCreatedAt());
                    reviewResponse.setUpdatedAt(review.getUpdatedAt());
                    
                    return reviewResponse;
                })
                .collect(Collectors.toList());
            response.setReviews(reviewResponses);
        } else {
            response.setReviews(new ArrayList<>());
        }

        return response;
    }

}
