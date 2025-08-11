package com.greenkitchen.portal.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.dtos.MenuMealReviewResponse;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.services.MenuMealAIService;

@Service
public class MenuMealAIServiceImpl implements MenuMealAIService {

	@Autowired
	private MenuMealRepository menuMealRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public List<MenuMealResponse> getAllMenuMeals() {
		return menuMealRepository.findAllActive().stream().map(this::toResponse).collect(Collectors.toList());
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
			List<MenuMealReviewResponse> reviewResponses = menuMeal.getReviews().stream().map(review -> {
				MenuMealReviewResponse reviewResponse = new MenuMealReviewResponse();
				reviewResponse.setId(review.getId());
				reviewResponse.setRating(review.getRating());
				reviewResponse.setComment(review.getComment());

				reviewResponse.setMenuMealId(menuMeal.getId());
				reviewResponse.setMenuMealTitle(menuMeal.getTitle());

				if (review.getCustomer() != null) {
					reviewResponse.setCustomerId(review.getCustomer().getId());
					reviewResponse.setCustomerName(
							review.getCustomer().getFirstName() + " " + review.getCustomer().getLastName());
				}

				reviewResponse.setCreatedAt(review.getCreatedAt());
				reviewResponse.setUpdatedAt(review.getUpdatedAt());

				return reviewResponse;
			}).collect(Collectors.toList());
			response.setReviews(reviewResponses);
		} else {
			response.setReviews(new ArrayList<>());
		}

		return response;
	}


}
