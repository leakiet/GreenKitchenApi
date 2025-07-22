package com.greenkitchen.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.SavedFoodMixRequest;
import com.greenkitchen.portal.dtos.SavedFoodMixResponse;
import com.greenkitchen.portal.services.SavedFoodMixService;

@CrossOrigin
@RestController
@RequestMapping("/apis/v1/saved-food-mix")
public class SavedFoodMixController {

    @Autowired
    private SavedFoodMixService savedFoodMixService;

    @GetMapping
    public ResponseEntity<List<SavedFoodMixResponse>> getAllSavedFoodMixes() {
        List<SavedFoodMixResponse> savedFoodMixes = savedFoodMixService.getAllSavedFoodMixes();
        return ResponseEntity.ok(savedFoodMixes);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<SavedFoodMixResponse>> getSavedFoodMixesByCustomerId(@PathVariable("customerId") Long customerId) {
        List<SavedFoodMixResponse> savedFoodMixes = savedFoodMixService.getSavedFoodMixesByCustomerId(customerId);
        return ResponseEntity.ok(savedFoodMixes);
    }

    @PostMapping
    public ResponseEntity<SavedFoodMixResponse> createSavedFoodMix(@RequestBody SavedFoodMixRequest request) {
        SavedFoodMixResponse savedFoodMix = savedFoodMixService.createSavedFoodMix(request);
        return ResponseEntity.ok(savedFoodMix);
    }
}
