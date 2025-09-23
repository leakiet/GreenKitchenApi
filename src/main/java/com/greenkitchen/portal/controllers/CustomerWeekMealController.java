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

import com.greenkitchen.portal.dtos.CustomerWeekMealDayResponse;
import com.greenkitchen.portal.dtos.CustomerWeekMealDayUpdateRequest;
import com.greenkitchen.portal.dtos.CustomerWeekMealRequest;
import com.greenkitchen.portal.dtos.CustomerWeekMealResponse;
import com.greenkitchen.portal.entities.CustomerWeekMeal;
import com.greenkitchen.portal.entities.CustomerWeekMealDay;
import com.greenkitchen.portal.services.CustomerWeekMealService;

@RestController
@RequestMapping("/apis/v1/customer-week-meals")
public class CustomerWeekMealController {

    @Autowired
    private CustomerWeekMealService customerWeekMealService;

    @PostMapping
    public ResponseEntity<?> createCustomerWeekMeal(@RequestBody CustomerWeekMealRequest request) {
        try {
            CustomerWeekMeal created = customerWeekMealService.createCustomerWeekMeal(request);
            CustomerWeekMealResponse response = customerWeekMealService.getCustomerWeekMealById(created.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Create customer week meal failed: " + e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerWeekMealsByCustomerId(@PathVariable("customerId") Long customerId) {
        try {
            List<CustomerWeekMealResponse> responses = customerWeekMealService.getCustomerWeekMealsByCustomerId(customerId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Get customer week meals failed: " + e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}/type/{type}")
    public ResponseEntity<?> getCustomerWeekMealsByCustomerIdAndType(
            @PathVariable("customerId") Long customerId,
            @PathVariable("type") String type) {
        try {
            List<CustomerWeekMealResponse> responses = customerWeekMealService.getCustomerWeekMealsByCustomerIdAndType(customerId, type);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Get customer week meals by type failed: " + e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}/type/{type}/date/{date}")
    public ResponseEntity<?> getCustomerWeekMealByCustomerIdAndTypeAndDate(
            @PathVariable("customerId") Long customerId,
            @PathVariable("type") String type,
            @PathVariable("date") String date) {
        try {
            CustomerWeekMealResponse response = customerWeekMealService.getCustomerWeekMealByCustomerIdAndTypeAndDate(customerId, type, date);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Get customer week meal by date failed: " + e.getMessage());
        }
    }

    @GetMapping("/{customerWeekMealId}")
    public ResponseEntity<?> getCustomerWeekMealById(@PathVariable("customerWeekMealId") Long customerWeekMealId) {
        try {
            CustomerWeekMealResponse response = customerWeekMealService.getCustomerWeekMealById(customerWeekMealId);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Get customer week meal failed: " + e.getMessage());
        }
    }

    @PutMapping("/{customerWeekMealId}")
    public ResponseEntity<?> updateCustomerWeekMeal(
            @PathVariable("customerWeekMealId") Long customerWeekMealId,
            @RequestBody CustomerWeekMealRequest request) {
        try {
            request.setId(customerWeekMealId);
            CustomerWeekMealResponse response = customerWeekMealService.updateCustomerWeekMeal(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Update customer week meal failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/{customerWeekMealId}")
    public ResponseEntity<?> deleteCustomerWeekMeal(@PathVariable("customerWeekMealId") Long customerWeekMealId) {
        try {
            customerWeekMealService.deleteCustomerWeekMeal(customerWeekMealId);
            return ResponseEntity.ok("Customer week meal deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Delete customer week meal failed: " + e.getMessage());
        }
    }

    @GetMapping("/{customerWeekMealId}/days/{dayId}")
    public ResponseEntity<?> getCustomerWeekMealDayById(
            @PathVariable("customerWeekMealId") Long customerWeekMealId,
            @PathVariable("dayId") Long dayId) {
        try {
            CustomerWeekMealDayResponse response = customerWeekMealService.getCustomerWeekMealDayById(customerWeekMealId, dayId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Day not found: " + e.getMessage());
        }
    }

    @PutMapping("/{customerWeekMealId}/days/{dayId}")
    public ResponseEntity<?> updateCustomerWeekMealDay(
            @PathVariable("customerWeekMealId") Long customerWeekMealId,
            @PathVariable("dayId") Long dayId,
            @RequestBody CustomerWeekMealDayUpdateRequest request) {
        try {
            CustomerWeekMealDay updated = customerWeekMealService.updateCustomerWeekMealDay(customerWeekMealId, dayId, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Update customer week meal day failed: " + e.getMessage());
        }
    }
}
