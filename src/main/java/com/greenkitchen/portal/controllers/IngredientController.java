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
import com.greenkitchen.portal.entities.NutritionInfo;
import com.greenkitchen.portal.services.IngredientService;
import com.greenkitchen.portal.utils.ImageUtils;

@RestController
@RequestMapping("/apis/v1/ingredients")
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

    @GetMapping()
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

    @PostMapping()
    public ResponseEntity<?> addIngredient(@ModelAttribute IngredientRequest ingredientRequest,
            @RequestParam("imageFile") MultipartFile file) {
        try {
            // Kiểm tra title duy nhất trước khi thêm
            if (ingredientService.existsByTitle(ingredientRequest.getTitle())) {
                return ResponseEntity.badRequest().body("Title already exists: " + ingredientRequest.getTitle());
            }

            Ingredients ingredient = modelMapper.map(ingredientRequest, Ingredients.class);
            NutritionInfo nutrition = new NutritionInfo();
            nutrition.setCalories(ingredientRequest.getCalories());
            nutrition.setProtein(ingredientRequest.getProtein());
            nutrition.setCarbs(ingredientRequest.getCarbs());
            nutrition.setFat(ingredientRequest.getFat());
            ingredient.setNutrition(nutrition);
            if (file != null && !file.isEmpty()) {
                String imageUrl = ImageUtils.uploadImage(file);
                ingredient.setImage(imageUrl);
            }
            Ingredients saved = ingredientService.save(ingredient);
            return ResponseEntity.status(201).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<?> updateIngredientImage(
            @PathVariable("id") Long id,
            @RequestParam("imageFile") MultipartFile file) {
        try {
            Ingredients existing = ingredientService.findById(id);
            if (existing == null) {
                return ResponseEntity.status(404).body("Ingredient not found");
            }
            String oldImage = existing.getImage();
            if (file != null && !file.isEmpty()) {
                if (oldImage != null && !oldImage.isEmpty()) {
                    ImageUtils.deleteImage(oldImage);
                }
                String imageUrl = ImageUtils.uploadImage(file);
                existing.setImage(imageUrl);
                ingredientService.update(existing);
                return ResponseEntity.ok("Image updated successfully");
            } else {
                return ResponseEntity.badRequest().body("No image file provided");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateIngredient(
            @PathVariable("id") Long id,
            @ModelAttribute IngredientRequest ingredientRequest) {
        try {
            Ingredients existing = ingredientService.findById(id);
            if (existing == null) {
                return ResponseEntity.status(404).body("Ingredient not found");
            }

            // Kiểm tra title duy nhất nếu title mới khác title cũ
            if (!existing.getTitle().equals(ingredientRequest.getTitle()) &&
                ingredientService.existsByTitle(ingredientRequest.getTitle())) {
                return ResponseEntity.badRequest().body("Title already exists: " + ingredientRequest.getTitle());
            }

            boolean isSame = existing.getTitle().equals(ingredientRequest.getTitle()) &&
                    existing.getType() == ingredientRequest.getType() &&
                    existing.getNutrition().getCalories().equals(ingredientRequest.getCalories()) &&
                    existing.getNutrition().getProtein().equals(ingredientRequest.getProtein()) &&
                    existing.getNutrition().getCarbs().equals(ingredientRequest.getCarbs()) &&
                    existing.getNutrition().getFat().equals(ingredientRequest.getFat());

            if (isSame) {
                return ResponseEntity.ok(existing);
            }

            modelMapper.map(ingredientRequest, existing);

            if (existing.getNutrition() == null) {
                existing.setNutrition(new NutritionInfo());
            }
            existing.getNutrition().setCalories(ingredientRequest.getCalories());
            existing.getNutrition().setProtein(ingredientRequest.getProtein());
            existing.getNutrition().setCarbs(ingredientRequest.getCarbs());
            existing.getNutrition().setFat(ingredientRequest.getFat());

            existing.setPrice(ingredientRequest.getPrice());
            existing.setStock(ingredientRequest.getStock());

            // Không xử lý file/image ở đây nữa

            Ingredients updated = ingredientService.update(existing);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable("id") Long id) {
        try {
            ingredientService.deleteById(id);
            return ResponseEntity.ok("Ingredient deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
