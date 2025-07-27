package com.greenkitchen.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.CustomMealRequest;
import com.greenkitchen.portal.dtos.CustomMealResponse;
import com.greenkitchen.portal.services.CustomMealService;

@RestController
@RequestMapping("/apis/v1/custom-meals")
public class CustomMealController {

    @Autowired
    private CustomMealService customMealService;

    @GetMapping
    public ResponseEntity<List<CustomMealResponse>> getAll() {
        return ResponseEntity.ok(customMealService.getAllCustomMeals());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomMealResponse>> getByCustomer(@PathVariable("customerId") Long customerId) {
        try {
            List<CustomMealResponse> res = customMealService.getCustomMealsByCustomerId(customerId);
            if (res.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            throw new RuntimeException("Custom meal not found with id: " + customerId);
        }
    }

    @PostMapping
    public ResponseEntity<CustomMealResponse> create(@RequestBody CustomMealRequest request) {
        try {
            if (request.getCustomerId() == null || request.getName() == null) {
                return ResponseEntity.badRequest().body(null);
            }
            return ResponseEntity.ok(customMealService.createCustomMeal(request));
        } catch (Exception e) {
            throw new RuntimeException("Invalid request data");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomMealResponse> update(@PathVariable("id") Long id,
            @RequestBody CustomMealRequest request) {
        try {
            if (request.getCustomerId() == null || request.getName() == null) {
                return ResponseEntity.badRequest().body(null);
            }
            return ResponseEntity.ok(customMealService.updateCustomMeal(id, request));
        } catch (Exception e) {
            throw new RuntimeException("Invalid request data");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Long id) {
        try {
            if (!customMealService.getAllCustomMeals().stream()
                    .anyMatch(meal -> meal.getId().equals(id))) {
                return ResponseEntity.notFound().build();
            }
            customMealService.deleteCustomMeal(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            throw new RuntimeException("Custom meal not found with id: " + id);
        }
    }
}
