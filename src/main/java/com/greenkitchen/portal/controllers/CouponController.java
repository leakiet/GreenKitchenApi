package com.greenkitchen.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
  @GetMapping("/{id}")
  public ResponseEntity<Coupon> getCouponById(@PathVariable("id") Long id) {
    try {
      Coupon coupon = couponService.getCouponById(id);
      return ResponseEntity.ok(coupon);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Lấy thông tin coupon theo code
   */
  @GetMapping("/code/{code}")
  public ResponseEntity<Coupon> getCouponByCode(@PathVariable("code") String code) {
    try {
      Coupon coupon = couponService.getCouponByCode(code);
      return ResponseEntity.ok(coupon);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * ADMIN: Lấy tất cả coupons (không phân trang)
   */
  @GetMapping("/admin/all")
  public ResponseEntity<List<Coupon>> getAllCoupons() {
    List<Coupon> coupons = couponService.getAllCoupons();
    return ResponseEntity.ok(coupons);
  }

  /**
   * ADMIN: Tạo coupon mới
   */
  @PostMapping("/admin/create")
  public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody Coupon coupon) {
    try {
      Coupon createdCoupon = couponService.createCoupon(coupon);
      return ResponseEntity.ok(createdCoupon);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * ADMIN: Cập nhật coupon
   */
  @PutMapping("/admin/update/{id}")
  public ResponseEntity<Coupon> updateCoupon(@PathVariable("id") Long id, @Valid @RequestBody Coupon coupon) {
    try {
      Coupon updatedCoupon = couponService.updateCoupon(id, coupon);
      return ResponseEntity.ok(updatedCoupon);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * ADMIN: Xóa coupon
   */
  @DeleteMapping("/admin/delete/{id}")
  public ResponseEntity<Void> deleteCoupon(@PathVariable("id") Long id) {
    try {
      couponService.deleteCoupon(id);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}