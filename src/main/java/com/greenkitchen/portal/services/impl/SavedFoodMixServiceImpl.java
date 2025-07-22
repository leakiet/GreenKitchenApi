package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.IngredientQuantity;
import com.greenkitchen.portal.dtos.IngredientQuantityResponse;
import com.greenkitchen.portal.dtos.IngredientResponse;
import com.greenkitchen.portal.dtos.SavedFoodMixRequest;
import com.greenkitchen.portal.dtos.SavedFoodMixResponse;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.IngredientQuantityEmbeddable;
import com.greenkitchen.portal.entities.Ingredients;
import com.greenkitchen.portal.entities.SavedFoodMix;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.IngredientRepository;
import com.greenkitchen.portal.repositories.SavedFoodMixRepository;
import com.greenkitchen.portal.services.SavedFoodMixService;

@Service
public class SavedFoodMixServiceImpl implements SavedFoodMixService {

    @Autowired
    private SavedFoodMixRepository savedFoodMixRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<SavedFoodMixResponse> getAllSavedFoodMixes() {
        List<SavedFoodMix> savedFoodMixes = savedFoodMixRepository.findAll();
        return savedFoodMixes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SavedFoodMixResponse> getSavedFoodMixesByCustomerId(Long customerId) {
        List<SavedFoodMix> savedFoodMixes = savedFoodMixRepository.findAllByCustomerId(customerId);
        return savedFoodMixes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SavedFoodMixResponse createSavedFoodMix(SavedFoodMixRequest request) {
        SavedFoodMix savedFoodMix = new SavedFoodMix();
        
        // Convert IngredientQuantity to IngredientQuantityEmbeddable
        savedFoodMix.setProteins(convertToEmbeddable(request.getProteins()));
        savedFoodMix.setCarbs(convertToEmbeddable(request.getCarbs()));
        savedFoodMix.setSides(convertToEmbeddable(request.getSides()));
        savedFoodMix.setSauces(convertToEmbeddable(request.getSauces()));
        savedFoodMix.setNote(request.getNote());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        savedFoodMix.setCustomer(customer);

        SavedFoodMix savedEntity = savedFoodMixRepository.save(savedFoodMix);
        return convertToResponse(savedEntity);
    }

    private SavedFoodMixResponse convertToResponse(SavedFoodMix savedFoodMix) {
        SavedFoodMixResponse response = new SavedFoodMixResponse();
        response.setId(savedFoodMix.getId());
        response.setCustomerId(savedFoodMix.getCustomer().getId());
        response.setNote(savedFoodMix.getNote());

        // Convert embeddable to response with ingredient details
        response.setProteins(convertToQuantityResponse(savedFoodMix.getProteins()));
        response.setCarbs(convertToQuantityResponse(savedFoodMix.getCarbs()));
        response.setSides(convertToQuantityResponse(savedFoodMix.getSides()));
        response.setSauces(convertToQuantityResponse(savedFoodMix.getSauces()));
        
        return response;
    }

    private List<IngredientQuantityEmbeddable> convertToEmbeddable(List<IngredientQuantity> ingredientQuantities) {
        if (ingredientQuantities == null || ingredientQuantities.isEmpty()) {
            return List.of();
        }
        
        return ingredientQuantities.stream()
                .map(iq -> new IngredientQuantityEmbeddable(iq.getIngredientId(), iq.getQuantity()))
                .collect(Collectors.toList());
    }

    private List<IngredientQuantityResponse> convertToQuantityResponse(List<IngredientQuantityEmbeddable> embeddables) {
        if (embeddables == null || embeddables.isEmpty()) {
            return List.of();
        }

        return embeddables.stream()
                .map(embeddable -> {
                    IngredientQuantityResponse response = new IngredientQuantityResponse();
                    response.setIngredientId(embeddable.getIngredientId());
                    response.setQuantity(embeddable.getQuantity());
                    
                    // Get ingredient details
                    ingredientRepository.findById(embeddable.getIngredientId())
                            .ifPresent(ingredient -> {
                                IngredientResponse ingredientResponse = new IngredientResponse();
                                BeanUtils.copyProperties(ingredient, ingredientResponse);
                                response.setIngredient(ingredientResponse);
                            });
                    
                    return response;
                })
                .collect(Collectors.toList());
    }
}
