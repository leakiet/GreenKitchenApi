package com.greenkitchen.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.ExchangeCouponRequest;
import com.greenkitchen.portal.entities.Coupon;
import com.greenkitchen.portal.services.CouponService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/apis/v1/coupons")
public class CouponController {

  @Autowired
  private CouponService couponService;

  /**
   * Hiển thị danh sách các coupon có thể đổi (đơn giản, không phân trang)
   */
  @GetMapping("/available")
  public ResponseEntity<List<Coupon>> getAvailableCoupons() {
    List<Coupon> coupons = couponService.getAvailableCouponsForExchange();
    return ResponseEntity.ok(coupons);
  }

  /**
   * Đổi điểm lấy coupon
   */
  @PostMapping("/exchange")
  public ResponseEntity<Coupon> exchangePointsForCoupon(@Valid @RequestBody ExchangeCouponRequest request) {
      Coupon coupon = couponService.exchangePointsForCoupon(request.getCustomerId(), request.getCouponId());
      return ResponseEntity.ok(coupon);
  }

  /**
   * Lấy thông tin chi tiết coupon
   */
  @GetMapping("/{couponId}")
  public ResponseEntity<Coupon> getCouponById(@PathVariable Long couponId) {
    try {
      Coupon coupon = couponService.getCouponById(couponId);
      return ResponseEntity.ok(coupon);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Lấy thông tin coupon theo code
   */
  @GetMapping("/code/{code}")
  public ResponseEntity<Coupon> getCouponByCode(@PathVariable String code) {
    try {
      Coupon coupon = couponService.getCouponByCode(code);
      return ResponseEntity.ok(coupon);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
