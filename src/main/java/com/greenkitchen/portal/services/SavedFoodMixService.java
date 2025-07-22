package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.dtos.SavedFoodMixRequest;
import com.greenkitchen.portal.dtos.SavedFoodMixResponse;

public interface SavedFoodMixService {
    List<SavedFoodMixResponse> getAllSavedFoodMixes();
    List<SavedFoodMixResponse> getSavedFoodMixesByCustomerId(Long customerId);
    SavedFoodMixResponse createSavedFoodMix(SavedFoodMixRequest request);
}
