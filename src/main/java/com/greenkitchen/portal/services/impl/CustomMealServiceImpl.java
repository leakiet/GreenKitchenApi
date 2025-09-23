package com.greenkitchen.portal.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.CustomMealDetailRequest;
import com.greenkitchen.portal.dtos.CustomMealDetailResponse;
import com.greenkitchen.portal.dtos.CustomMealRequest;
import com.greenkitchen.portal.dtos.CustomMealResponse;
import com.greenkitchen.portal.entities.CustomMeal;
import com.greenkitchen.portal.entities.CustomMealDetail;
import com.greenkitchen.portal.entities.NutritionInfo;
import com.greenkitchen.portal.repositories.CustomMealRepository;
import com.greenkitchen.portal.repositories.IngredientRepository;
import com.greenkitchen.portal.services.CustomMealService;

@Service
public class CustomMealServiceImpl implements CustomMealService {

  @Autowired
  private CustomMealRepository customMealRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private IngredientRepository ingredientRepository; // hoặc IngredientService nếu có

  @Override
  public List<CustomMealResponse> getAllCustomMeals() {
    return customMealRepository.findAllByIsDeletedFalse().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  public List<CustomMealResponse> getCustomMealsByCustomerId(Long customerId) {
    return customMealRepository.findAllByCustomerIdAndIsDeletedFalse(customerId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  public CustomMealResponse findById(Long id) {
    CustomMeal meal = customMealRepository.findById(id)
        .filter(m -> !m.getIsDeleted())
        .orElseThrow(() -> new RuntimeException("Custom meal not found with id: " + id));
    return toResponse(meal);
  }

  @Override
  public CustomMealResponse createCustomMeal(CustomMealRequest request) {
    CustomMeal meal = new CustomMeal();
    meal.setCustomerId(request.getCustomerId());
    meal.setTitle(request.getTitle());

    if (meal.getNutrition() == null) {
      meal.setNutrition(new NutritionInfo());
    }
    meal.setPrice(request.getPrice());
    meal.setDescription(request.getDescription());
    meal.setImage(request.getImage());
    meal.getNutrition().setCalories(request.getCalories());
    meal.getNutrition().setProtein(request.getProtein());
    meal.getNutrition().setCarbs(request.getCarb());
    meal.getNutrition().setFat(request.getFat());

    List<CustomMealDetail> details = new ArrayList<>();

    if (request.getProteins() != null) {
      details.addAll(request.getProteins().stream()
          .map(d -> createDetail(d, meal))
          .collect(Collectors.toList()));
    }

    if (request.getCarbs() != null) {
      details.addAll(request.getCarbs().stream()
          .map(d -> createDetail(d, meal))
          .collect(Collectors.toList()));
    }

    if (request.getSides() != null) {
      details.addAll(request.getSides().stream()
          .map(d -> createDetail(d, meal))
          .collect(Collectors.toList()));
    }

    if (request.getSauces() != null) {
      details.addAll(request.getSauces().stream()
          .map(d -> createDetail(d, meal))
          .collect(Collectors.toList()));
    }

    meal.setDetails(details);
    CustomMeal saved = customMealRepository.save(meal);
    return toResponse(saved);
  }

  private CustomMealDetail createDetail(CustomMealDetailRequest request, CustomMeal meal) {
    CustomMealDetail detail = new CustomMealDetail();
    detail.setIngredientId(request.getIngredientId());
    detail.setQuantity(request.getQuantity());
    detail.setCustomMeal(meal);
    return detail;
  }

  @Override
  public CustomMealResponse updateCustomMeal(Long id, CustomMealRequest request) {
    CustomMeal existingMeal = customMealRepository.findById(id)
        .filter(m -> !m.getIsDeleted())
        .orElseThrow(() -> new RuntimeException("Custom meal not found with id: " + id));

    existingMeal.setCustomerId(request.getCustomerId());
    existingMeal.setTitle(request.getTitle());

    if (existingMeal.getNutrition() == null) {
      existingMeal.setNutrition(new NutritionInfo());
    }
    existingMeal.setPrice(request.getPrice());
    existingMeal.setDescription(request.getDescription());
    existingMeal.setImage(request.getImage());
    existingMeal.getNutrition().setCalories(request.getCalories());
    existingMeal.getNutrition().setProtein(request.getProtein());
    existingMeal.getNutrition().setCarbs(request.getCarb());
    existingMeal.getNutrition().setFat(request.getFat());

    // Thay vì clear() và tạo mới, hãy remove từng element một cách an toàn
    if (existingMeal.getDetails() != null) {
      // Clear existing details an toàn
      existingMeal.getDetails().removeAll(existingMeal.getDetails());
    } else {
      existingMeal.setDetails(new ArrayList<>());
    }

    // Tạo new details
    List<CustomMealDetail> newDetails = new ArrayList<>();

    if (request.getProteins() != null) {
      newDetails.addAll(request.getProteins().stream()
          .map(d -> createDetail(d, existingMeal))
          .collect(Collectors.toList()));
    }

    if (request.getCarbs() != null) {
      newDetails.addAll(request.getCarbs().stream()
          .map(d -> createDetail(d, existingMeal))
          .collect(Collectors.toList()));
    }

    if (request.getSides() != null) {
      newDetails.addAll(request.getSides().stream()
          .map(d -> createDetail(d, existingMeal))
          .collect(Collectors.toList()));
    }

    if (request.getSauces() != null) {
      newDetails.addAll(request.getSauces().stream()
          .map(d -> createDetail(d, existingMeal))
          .collect(Collectors.toList()));
    }

    // Add new details to existing collection
    existingMeal.getDetails().addAll(newDetails);

    CustomMeal saved = customMealRepository.save(existingMeal);
    return toResponse(saved);
  }

  @Override
  public void deleteCustomMeal(Long id) {
    CustomMeal meal = customMealRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Custom meal not found with id: " + id));
    meal.setIsDeleted(true);
    customMealRepository.save(meal);
  }

  public CustomMealResponse toResponse(CustomMeal meal) {
    CustomMealResponse response = modelMapper.map(meal, CustomMealResponse.class);

    if (meal.getNutrition() != null) {
      response.setCalories(meal.getNutrition().getCalories());
      response.setProtein(meal.getNutrition().getProtein());
      response.setCarb(meal.getNutrition().getCarbs());
      response.setFat(meal.getNutrition().getFat());
    }


    if (meal.getDetails() != null) {
      List<CustomMealDetailResponse> detailResponses = meal.getDetails().stream()
          .map(d -> {
            CustomMealDetailResponse detailResponse = new CustomMealDetailResponse();
            detailResponse.setQuantity(d.getQuantity());

            ingredientRepository.findById(d.getIngredientId())
                .ifPresent(ingredient -> {
                  detailResponse.setId(ingredient.getId());
                  detailResponse.setTitle(ingredient.getTitle());
                  detailResponse.setType(ingredient.getType());
                  detailResponse.setDescription(ingredient.getDescription());
                  detailResponse.setImage(ingredient.getImage());

                  if (ingredient.getNutrition() != null) {
                    detailResponse.setCalories(ingredient.getNutrition().getCalories());
                    detailResponse.setProtein(ingredient.getNutrition().getProtein());
                    detailResponse.setCarbs(ingredient.getNutrition().getCarbs());
                    detailResponse.setFat(ingredient.getNutrition().getFat());
                  }
                });
            return detailResponse;
          })
          .collect(Collectors.toList());
      response.setDetails(detailResponses);
    }
    return response;
  }
}
