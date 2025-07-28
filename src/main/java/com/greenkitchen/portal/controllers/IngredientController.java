package com.greenkitchen.portal.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.greenkitchen.portal.dtos.IngredientRequest;
import com.greenkitchen.portal.dtos.IngredientResponse;
import com.greenkitchen.portal.entities.Ingredients;
import com.greenkitchen.portal.services.IngredientService;
import com.greenkitchen.portal.utils.ImageUtils;

@RestController
@RequestMapping("/apis/v1")
public class IngredientController {
    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private ImageUtils ImageUtils;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    public IngredientController(ImageUtils ImageUtils) {
        this.ImageUtils = ImageUtils;
    }

    @GetMapping("/customers/ingredients")
    public ResponseEntity<Map<String, List<IngredientResponse>>> getAllIngredientsGrouped() {
        List<Ingredients> all = ingredientService.findAll();
        List<IngredientResponse> responses = all.stream().map(ingredient -> {
            IngredientResponse response = new IngredientResponse();
            modelMapper.map(ingredient, response);
            if (ingredient.getNutrition() != null) {
                modelMapper.map(ingredient.getNutrition(), response);
            } else {
                response.setCalories(0.0);
                response.setProtein(0.0);
                response.setCarbs(0.0);
                response.setFat(0.0);
            }
            return response;
        }).toList();

        Map<String, List<IngredientResponse>> grouped = responses.stream()
                .collect(Collectors.groupingBy(r -> {
                    String type = r.getType().toString();
                    return type == null ? "unknown" : type.toLowerCase();
                }));

        return ResponseEntity.ok(grouped);
    }

    @PostMapping("/ingredients")
    public ResponseEntity<Ingredients> addIngredient(@ModelAttribute IngredientRequest ingredientRequest,
            @RequestParam("imageFile") MultipartFile file) {

        Ingredients ingredient = modelMapper.map(ingredientRequest, Ingredients.class);
        if (file != null && !file.isEmpty()) {
            String imageUrl = ImageUtils.uploadImage(file);
            ingredient.setImage(imageUrl);
        }
        Ingredients saved = ingredientService.save(ingredient);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/ingredients/{id}")
    public ResponseEntity<Ingredients> updateIngredient(
            @PathVariable("id") Long id,
            @ModelAttribute IngredientRequest ingredientRequest,
            @RequestParam("imageFile") MultipartFile file) {

        Ingredients existing = ingredientService.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isSame = existing.getTitle().equals(ingredientRequest.getTitle()) &&
                existing.getType() == ingredientRequest.getType() &&
                existing.getNutrition().getCalories().equals(ingredientRequest.getCalories()) &&
                existing.getNutrition().getProtein().equals(ingredientRequest.getProtein()) &&
                existing.getNutrition().getCarbs().equals(ingredientRequest.getCarbs()) &&
                existing.getNutrition().getFat().equals(ingredientRequest.getFat()) &&
                (file == null || file.isEmpty());

        if (isSame) {
            return ResponseEntity.ok(existing);
        }

        modelMapper.map(ingredientRequest, existing);

        if (file != null && !file.isEmpty()) {
            if (existing.getImage() != null && !existing.getImage().isEmpty()) {
                ImageUtils.deleteImage(existing.getImage());
            }
            String imageUrl = ImageUtils.uploadImage(file);
            existing.setImage(imageUrl);
        }

        Ingredients updated = ingredientService.update(existing);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/customers/ingredients/{id}")
    public ResponseEntity<IngredientResponse> findById(@PathVariable("id") Long id) {
        Ingredients ingredient = ingredientService.findById(id);
        if (ingredient == null) {
            return ResponseEntity.notFound().build();
        }
        IngredientResponse response = new IngredientResponse();
        modelMapper.map(ingredient, response);

        if (ingredient.getNutrition() != null) {
            modelMapper.map(ingredient.getNutrition(), response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/ingredients/{id}")
    public ResponseEntity<String> deleteById(@PathVariable("id") Long id) {
        ingredientService.deleteById(id);
        return ResponseEntity.ok("Ingredient deleted successfully");
    }
}
