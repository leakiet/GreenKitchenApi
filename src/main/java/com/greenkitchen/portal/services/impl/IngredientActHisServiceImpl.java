package com.greenkitchen.portal.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.entities.IngredientActHis;
import com.greenkitchen.portal.repositories.IngredientActHisRepository;
import com.greenkitchen.portal.services.IngredientActHisService;

@Service
public class IngredientActHisServiceImpl implements IngredientActHisService {

  @Autowired
  private IngredientActHisRepository ingredientActHisRepository;

  
  @Override
  public List<IngredientActHis> findAll() {
    return ingredientActHisRepository.findAll();
  }

  @Override
  public IngredientActHis save(IngredientActHis ingredientActHis) {
    return ingredientActHisRepository.save(ingredientActHis);

  }

  @Override
  public List<IngredientActHis> findByCustomerId(Long customerId) {
    List<IngredientActHis> result = ingredientActHisRepository.findAllByCustomerId(customerId);
    if (result == null || result.isEmpty()) {
        throw new RuntimeException("IngredientActionHistory not found with customerId: " + customerId);
    }
    return result;
  }

}
