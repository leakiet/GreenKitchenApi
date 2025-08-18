package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.greenkitchen.portal.dtos.CustomerReferenceRequest;
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
        }
    }

    /**
     * Tạo mới CustomerReference (sử dụng DTO)
     */
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCustomerReference(@RequestBody CustomerReferenceRequest request) {
        try {
            CustomerReference createdReference = customerReferenceService.createCustomerReference(request);
            return ResponseEntity.ok(createdReference);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }

    /**
     * Cập nhật CustomerReference (sử dụng DTO)
     */
    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCustomerReference(@RequestBody CustomerReferenceRequest request) {
        try {
            CustomerReference updatedReference = customerReferenceService.updateCustomerReference(request);
            return ResponseEntity.ok(updatedReference);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }
}
