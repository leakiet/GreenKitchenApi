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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.MenuMealReviewRequest;
import com.greenkitchen.portal.dtos.MenuMealReviewResponse;
import com.greenkitchen.portal.dtos.PagedResponse;
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

    @PostMapping
    public ResponseEntity<?> createMenuMealReview(@Valid @RequestBody MenuMealReviewRequest request) {
        try {
            MenuMealReviewResponse review = menuMealReviewService.createMenuMealReview(request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenuMealReview(@PathVariable("id") Long id,
            @Valid @RequestBody MenuMealReviewRequest request) {
        try {
            MenuMealReviewResponse review = menuMealReviewService.updateMenuMealReview(id, request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenuMealReview(@PathVariable("id") Long id) {
        try {
            menuMealReviewService.deleteMenuMealReview(id);
            return ResponseEntity.ok("Review deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/menu-meal/{menuMealId}")
    public ResponseEntity<?> getReviewsByMenuMeal(@PathVariable("menuMealId") Long menuMealId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "q", required = false) String q) {
        if (page != null && size != null) {
            PagedResponse<MenuMealReviewResponse> res = menuMealReviewService.getPagedReviewsByMenuMealId(menuMealId, page, size, status, q);
            return ResponseEntity.ok(res);
        }
        List<MenuMealReviewResponse> reviews = menuMealReviewService.getAllReviewsByMenuMealId(menuMealId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<MenuMealReviewResponse>> getReviewsByCustomer(
            @PathVariable("customerId") Long customerId) {
        List<MenuMealReviewResponse> reviews = menuMealReviewService.getAllReviewsByCustomerId(customerId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> listFilteredOrders(@RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "q", required = false) String q) {
        if (page != null && size != null) {
            // Sửa call để xóa fromDate và toDate
            PagedResponse<MenuMealReviewResponse> res = menuMealReviewService.listFilteredPaged(page, size, status, q);
            return ResponseEntity.ok(res);
        }
        List<MenuMealReviewResponse> list = menuMealReviewService.listAll();
        return ResponseEntity.ok(list);
    }

}
