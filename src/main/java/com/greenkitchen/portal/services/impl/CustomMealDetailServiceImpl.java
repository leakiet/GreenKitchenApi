package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.CustomMealDetailRequest;
import com.greenkitchen.portal.dtos.CustomMealDetailResponse;
import com.greenkitchen.portal.entities.CustomMeal;
import com.greenkitchen.portal.entities.CustomMealDetail;
import com.greenkitchen.portal.repositories.CustomMealDetailRepository;
import com.greenkitchen.portal.repositories.CustomMealRepository;
import com.greenkitchen.portal.services.CustomMealDetailService;

@Service
public class CustomMealDetailServiceImpl implements CustomMealDetailService {

  @Autowired
  private CustomMealDetailRepository detailRepository;

  @Autowired
  private CustomMealRepository customMealRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public List<CustomMealDetailResponse> getDetailsByCustomMealId(Long customMealId) {
    return detailRepository.findAll().stream()
        .filter(d -> d.getCustomMeal().getId().equals(customMealId))
        .map(d -> modelMapper.map(d, CustomMealDetailResponse.class))
        .collect(Collectors.toList());
  }

  @Override
  public CustomMealDetailResponse getDetailById(Long id) {
    CustomMealDetail detail = detailRepository.findById(id).orElseThrow();
    return modelMapper.map(detail, CustomMealDetailResponse.class);
  }

  @Override
  public CustomMealDetailResponse addDetail(Long customMealId, CustomMealDetailRequest request) {
    CustomMeal customMeal = customMealRepository.findById(customMealId).orElseThrow();
    CustomMealDetail detail = modelMapper.map(request, CustomMealDetail.class);
    detail.setCustomMeal(customMeal);
    CustomMealDetail saved = detailRepository.save(detail);
    return modelMapper.map(saved, CustomMealDetailResponse.class);
  }

  @Override
  public CustomMealDetailResponse updateDetail(Long id, CustomMealDetailRequest request) {
    CustomMealDetail detail = detailRepository.findById(id).orElseThrow();
    detail.setIngredientId(request.getIngredientId());
    detail.setQuantity(request.getQuantity());
    CustomMealDetail saved = detailRepository.save(detail);
    return modelMapper.map(saved, CustomMealDetailResponse.class);
  }

  @Override
  public void deleteDetail(Long id) {
    detailRepository.deleteById(id);
  }
}
