package com.greenkitchen.portal.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.entities.Ingredients;
import com.greenkitchen.portal.repositories.IngredientRepository;
import com.greenkitchen.portal.services.IngredientService;

@Service
public class IngredientServiceImpl implements IngredientService {

    @Autowired
    private final IngredientRepository ingredientRepository;

    public IngredientServiceImpl(IngredientRepository ingredientsRepository) {
        this.ingredientRepository = ingredientsRepository;
    }

    @Override
    public boolean existsByTitle(String title) {
        return ingredientRepository.existsByTitle(title);
    }

    @Override
    public List<Ingredients> findAll() {
        return ingredientRepository.findAll();
    }

    @Override
    public Ingredients save(Ingredients ingredient) {
        return ingredientRepository.save(ingredient);
    }

    @Override
    public Ingredients findById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Ingredient not found with id: " + id);
        }
        ingredientRepository.deleteById(id);
    }

    @Override
    public Ingredients update(Ingredients ingredient) {
        if (!ingredientRepository.existsById(ingredient.getId())) {
            throw new RuntimeException("Ingredient not found with id: " + ingredient.getId());
        }
        return ingredientRepository.save(ingredient);
    }
}
