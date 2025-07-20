package com.greenkitchen.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.greenkitchen.portal.entities.Ingredients;

@Service
public interface IngredientService {
    List<Ingredients> findAll();
    Ingredients save(Ingredients ingredient);
    Ingredients findById(Long id);
    void deleteById(Long id);
    Ingredients update(Ingredients ingredient);
}
