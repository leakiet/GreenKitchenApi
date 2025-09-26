package com.greenkitchen.portal.controllers;

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

import com.greenkitchen.portal.dtos.WeekMealDayResponse;
import com.greenkitchen.portal.dtos.WeekMealDayUpdateRequest;
import com.greenkitchen.portal.dtos.WeekMealRequest;
import com.greenkitchen.portal.dtos.WeekMealResponse;
import com.greenkitchen.portal.entities.WeekMealDay;
import com.greenkitchen.portal.services.WeekMealService;

@RestController
@RequestMapping("/apis/v1/week-meals")
public class WeekMealController {

  @Autowired
  private WeekMealService weekMealService;

  @PostMapping
  public ResponseEntity<?> createWeekMeal(@RequestBody WeekMealRequest request) {
    try {
      WeekMealResponse created = weekMealService.createWeekMeal(request);
      return ResponseEntity.ok(created);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("create week meal failed: " + e.getMessage());
    }
  }

  @GetMapping("/{weekMealId}/days/{dayId}")
  public ResponseEntity<?> getWeekMealDayById(
      @PathVariable("weekMealId") Long weekMealId,
      @PathVariable("dayId") Long dayId) {
    try {
      WeekMealDayResponse response = weekMealService.getWeekMealDayById(weekMealId, dayId);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(404).body("Day not found: " + e.getMessage());
    }
  }

  @PutMapping("/{weekMealId}/days/{dayId}")
  public ResponseEntity<?> updateWeekMealDay(
      @PathVariable("weekMealId") Long weekMealId,
      @PathVariable("dayId") Long dayId,
      @RequestBody WeekMealDayUpdateRequest request) {
    try {
      weekMealService.updateWeekMealDay(weekMealId, dayId, request);
      // Trả về response DTO thay vì entity để tránh lazy loading issues
      WeekMealDayResponse response = weekMealService.getWeekMealDayById(weekMealId, dayId);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(400).body("Update day failed: " + e.getMessage());
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
