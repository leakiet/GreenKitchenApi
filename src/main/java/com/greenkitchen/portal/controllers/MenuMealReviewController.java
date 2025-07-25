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
        System.out.println("=== GET Review by ID: " + id);
        MenuMealReview review = menuMealReviewService.getMenuMealReviewById(id);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(review);
    }
    
    @PostMapping
    public ResponseEntity<MenuMealReview> createMenuMealReview(@Valid @RequestBody MenuMealReviewRequest request) {
        try {
            MenuMealReview review = menuMealReviewService.createMenuMealReview(request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MenuMealReview> updateMenuMealReview(@PathVariable("id") Long id, 
            @Valid @RequestBody MenuMealReviewRequest request) {
        System.out.println("=== UPDATE Review ID: " + id);
        try {
            MenuMealReview review = menuMealReviewService.updateMenuMealReview(id, request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            System.out.println("=== ERROR: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMenuMealReview(@PathVariable("id") Long id) {
        System.out.println("=== DELETE Review ID: " + id);
        try {
            menuMealReviewService.deleteMenuMealReview(id);
            return ResponseEntity.ok("Review deleted successfully");
        } catch (RuntimeException e) {
            System.out.println("=== ERROR: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/menu-meal/{menuMealId}")
    public ResponseEntity<List<MenuMealReview>> getReviewsByMenuMeal(@PathVariable("menuMealId") Long menuMealId) {
        System.out.println("=== GET Reviews by MenuMeal ID: " + menuMealId);
        List<MenuMealReview> reviews = menuMealReviewService.getAllReviewsByMenuMealId(menuMealId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<MenuMealReview>> getReviewsByCustomer(@PathVariable("customerId") Long customerId) {
        System.out.println("=== GET Reviews by Customer ID: " + customerId);
        List<MenuMealReview> reviews = menuMealReviewService.getAllReviewsByCustomerId(customerId);
        return ResponseEntity.ok(reviews);
    }
}
