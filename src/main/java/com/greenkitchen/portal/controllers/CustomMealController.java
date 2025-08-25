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
    public ResponseEntity<?> getByCustomer(@PathVariable("customerId") Long customerId) {
        try {
            List<CustomMealResponse> res = customMealService.getCustomMealsByCustomerId(customerId);
            if (res.isEmpty()) {
                return ResponseEntity.status(404).body("No custom meals found for customer id: " + customerId);
            }
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CustomMealRequest request) {
        try {
            if (request.getCustomerId() == null || request.getTitle() == null) {
                return ResponseEntity.badRequest().body("CustomerId and Title are required");
            }
            CustomMealResponse created = customMealService.createCustomMeal(request);
            return ResponseEntity.status(201).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
            @RequestBody CustomMealRequest request) {
        try {
            if (request.getCustomerId() == null || request.getTitle() == null) {
                return ResponseEntity.badRequest().body("CustomerId and Title are required");
            }
            CustomMealResponse updated = customMealService.updateCustomMeal(id, request);
            if (updated == null) {
                return ResponseEntity.status(404).body("Custom meal not found with id: " + id);
            }
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        try {
            boolean exists = customMealService.getAllCustomMeals().stream()
                    .anyMatch(meal -> meal.getId().equals(id));
            if (!exists) {
                return ResponseEntity.status(404).body("Custom meal not found with id: " + id);
            }
            customMealService.deleteCustomMeal(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
