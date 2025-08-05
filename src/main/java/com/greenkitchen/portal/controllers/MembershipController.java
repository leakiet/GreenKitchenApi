package com.greenkitchen.portal.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerMembership;
import com.greenkitchen.portal.entities.PointHistory;
import com.greenkitchen.portal.enums.MembershipTier;
import com.greenkitchen.portal.repositories.PointHistoryRepository;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.services.MembershipService;
import com.greenkitchen.portal.dtos.UsePointsRequest;

@RestController
@RequestMapping("/apis/v1/membership")
public class MembershipController {
    
    @Autowired
    private MembershipService membershipService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    
    /**
     * Lấy thông tin membership của customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerMembership> getCustomerMembership(@PathVariable("customerId") Long customerId) {
        try {
            Customer customer = customerService.findById(customerId);
            CustomerMembership membership = membershipService.getCurrentMembership(customer);
            return ResponseEntity.ok(membership);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Lấy hạng membership của customer
     */
    @GetMapping("/tier/{customerId}")
    public ResponseEntity<MembershipTier> getCustomerTier(@PathVariable("customerId") Long customerId) {
        try {
            Customer customer = customerService.findById(customerId);
            MembershipTier tier = membershipService.getCustomerTier(customer);
            return ResponseEntity.ok(tier);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Lấy lịch sử điểm của customer
     */
    @GetMapping("/points-history/{customerId}")
    public ResponseEntity<List<PointHistory>> getPointHistory(@PathVariable("customerId") Long customerId) {
        try {
            Customer customer = customerService.findById(customerId);
            List<PointHistory> history = pointHistoryRepository.findByCustomerOrderByEarnedAtDesc(customer);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Sử dụng điểm thưởng
     */
    @PostMapping("/use-points")
    public ResponseEntity<String> usePoints(@RequestBody UsePointsRequest request) {
        try {
            Customer customer = customerService.findById(request.getCustomerId());
            boolean success = membershipService.usePoints(customer, request.getPointsToUse(), request.getDescription());
            
            if (success) {
                return ResponseEntity.ok("Points used successfully");
            } else {
                return ResponseEntity.badRequest().body("Insufficient points");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error using points: " + e.getMessage());
        }
    }
    
}
