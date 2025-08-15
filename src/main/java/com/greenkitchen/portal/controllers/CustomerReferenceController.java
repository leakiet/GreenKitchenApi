package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.greenkitchen.portal.entities.CustomerReference;
import com.greenkitchen.portal.services.CustomerReferenceService;

import java.util.List;

@RestController
@RequestMapping("/apis/v1/customer-references")
public class CustomerReferenceController {

    @Autowired
    private CustomerReferenceService customerReferenceService;

    /**
     * Lấy danh sách CustomerReference theo Customer ID
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerReferencesByCustomerId(@PathVariable("customerId") Long customerId) {
        try {
            List<CustomerReference> customerReferences = customerReferenceService
                    .getCustomerReferencesByCustomerId(customerId);
            return ResponseEntity.ok(customerReferences);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Tạo mới CustomerReference
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCustomerReference(@RequestBody CustomerReference customerReference) {
        try {
            CustomerReference createdReference = customerReferenceService.createCustomerReference(customerReference);
            return ResponseEntity.ok(createdReference);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Log for debugging
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Cập nhật CustomerReference
     */
    @PutMapping
    public ResponseEntity<?> updateCustomerReference(@RequestBody CustomerReference customerReference) {
        try {
            CustomerReference updatedReference = customerReferenceService.updateCustomerReference(customerReference);
            return ResponseEntity.ok(updatedReference);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Log for debugging
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Tạo mới hoặc cập nhật CustomerReference (upsert)
     */
    @PostMapping("/upsert")
    public ResponseEntity<?> createOrUpdateCustomerReference(@RequestBody CustomerReference customerReference) {
        try {
            CustomerReference result = customerReferenceService.createOrUpdateCustomerReference(customerReference);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Log for debugging
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}
