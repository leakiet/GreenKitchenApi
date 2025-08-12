package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.UseCouponRequest;
import com.greenkitchen.portal.entities.CustomerCoupon;
import com.greenkitchen.portal.services.CustomerCouponService;

import jakarta.validation.Valid;

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
  
  }