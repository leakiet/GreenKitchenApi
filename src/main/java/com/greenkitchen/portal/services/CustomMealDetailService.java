package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.dtos.CustomMealDetailRequest;
import com.greenkitchen.portal.dtos.CustomMealDetailResponse;

public interface CustomMealDetailService {
  List<CustomMealDetailResponse> getDetailsByCustomMealId(Long customMealId);

  CustomMealDetailResponse getDetailById(Long id);

  CustomMealDetailResponse addDetail(Long customMealId, CustomMealDetailRequest request);

  CustomMealDetailResponse updateDetail(Long id, CustomMealDetailRequest request);

  void deleteDetail(Long id);
}
