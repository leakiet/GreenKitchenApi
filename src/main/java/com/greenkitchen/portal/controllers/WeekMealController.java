package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.greenkitchen.portal.dtos.WeekMealRequest;
import com.greenkitchen.portal.entities.WeekMeal;
import com.greenkitchen.portal.services.WeekMealService;

@RestController
@RequestMapping("/apis/v1/week-meals")
public class WeekMealController {

  @Autowired
  private WeekMealService weekMealService;

  @PostMapping
  public ResponseEntity<?> createWeekMeal(@RequestBody WeekMealRequest request) {
    try {
      WeekMeal created = weekMealService.createWeekMeal(request);
      return ResponseEntity.ok(created);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("create week meal failed: " + e.getMessage());
    }
  }

  @GetMapping
  public ResponseEntity<?> getWeekMeal(
      @RequestParam("type") String type,
      @RequestParam("date") String date) {
    try {
      var response = weekMealService.getWeekMealByTypeAndDate(type, date);
      if (response == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("get week meal failed: " + e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getWeekMealById(@PathVariable("id") Long id) {
    var response = weekMealService.getWeekMealById(id);
    if (response == null)
      return ResponseEntity.notFound().build();
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteWeekMeal(@PathVariable("id") Long id) {
    try {
      weekMealService.deleteWeekMeal(id);
      return ResponseEntity.ok("Deleted successfully");
    } catch (Exception e) {
      return ResponseEntity.status(404).body(e.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateWeekMeal(@PathVariable("id") Long id, @RequestBody WeekMealRequest request) {
    try {
      request.setId(id);
      var updated = weekMealService.updateWeekMeal(request);
      return ResponseEntity.ok(updated);
    } catch (Exception e) {
      return ResponseEntity.status(400).body("Update failed: " + e.getMessage());
    }
  }

}
