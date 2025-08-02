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
            List<CustomerReference> customerReferences = customerReferenceService.getCustomerReferencesByCustomerId(customerId);
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
    @PostMapping
    public ResponseEntity<?> createCustomerReference(@RequestBody CustomerReference customerReference) {
        try {
            CustomerReference createdReference = customerReferenceService.createCustomerReference(customerReference);
            return ResponseEntity.ok(createdReference);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}
