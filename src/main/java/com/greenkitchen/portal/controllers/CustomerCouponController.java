package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.CustomerCouponIdsResponse;
import com.greenkitchen.portal.dtos.UseCouponRequest;
import com.greenkitchen.portal.entities.CustomerCoupon;
import com.greenkitchen.portal.services.CustomerCouponService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/apis/v1/customer-coupons")
public class CustomerCouponController {

    @Autowired
    private CustomerCouponService customerCouponService;

    //Update customer coupon
    @PutMapping("/use-coupon")
    public ResponseEntity<CustomerCoupon> useCoupon(@Valid @RequestBody UseCouponRequest useCouponRequest) {
        CustomerCoupon updatedCoupon = customerCouponService.customerUsedCoupon(useCouponRequest);
        return ResponseEntity.ok(updatedCoupon);
    }

    /**
     * ADMIN: Lấy danh sách customer IDs theo coupon ID
     */
    @GetMapping("/coupon/{couponId}")
    public ResponseEntity<CustomerCouponIdsResponse> getCustomerIdsByCouponId(@PathVariable("couponId") Long couponId) {
        CustomerCouponIdsResponse response = customerCouponService.getCustomerIdsByCouponId(couponId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy danh sách customer coupons có sẵn cho customer
     */
    @GetMapping("/customer/{customerId}/available")
    public ResponseEntity<List<CustomerCoupon>> getAvailableCustomerCoupons(@PathVariable("customerId") Long customerId) {
        List<CustomerCoupon> customerCoupons = customerCouponService.getAvailableCustomerCoupons(customerId);
        return ResponseEntity.ok(customerCoupons);
    }
}