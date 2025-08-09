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

import com.greenkitchen.portal.dtos.MenuMealReviewRequest;
import com.greenkitchen.portal.dtos.MenuMealReviewResponse;
import com.greenkitchen.portal.entities.MenuMealReview;
import com.greenkitchen.portal.services.MenuMealReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/apis/v1/menu-meal-reviews")
public class MenuMealReviewController {
    
    @Autowired
    private MenuMealReviewService menuMealReviewService;
    
    @GetMapping("/{id}")
    public ResponseEntity<MenuMealReview> getMenuMealReviewById(@PathVariable("id") Long id) {
        MenuMealReview review = menuMealReviewService.getMenuMealReviewById(id);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(review);
    }

    @PostMapping("/customers")
    public ResponseEntity<?> createMenuMealReview(@Valid @RequestBody MenuMealReviewRequest request) {
        try {
            MenuMealReviewResponse review = menuMealReviewService.createMenuMealReview(request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<?> updateMenuMealReview(@PathVariable("id") Long id,
            @Valid @RequestBody MenuMealReviewRequest request) {
        try {
            MenuMealReviewResponse review = menuMealReviewService.updateMenuMealReview(id, request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteMenuMealReview(@PathVariable("id") Long id) {
        try {
            menuMealReviewService.deleteMenuMealReview(id);
            return ResponseEntity.ok("Review deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/menu-meal/{menuMealId}") 
    public ResponseEntity<List<MenuMealReviewResponse>> getReviewsByMenuMeal(@PathVariable("menuMealId") Long menuMealId) {
        List<MenuMealReviewResponse> reviews = menuMealReviewService.getAllReviewsByMenuMealId(menuMealId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/customer/{customerId}") 
    public ResponseEntity<List<MenuMealReviewResponse>> getReviewsByCustomer(@PathVariable("customerId") Long customerId) {
        List<MenuMealReviewResponse> reviews = menuMealReviewService.getAllReviewsByCustomerId(customerId);
        return ResponseEntity.ok(reviews);
    }
}
