package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.entities.Coupon;

public interface CouponService {
    
    /**
     * Lấy danh sách tất cả coupon có thể đổi (cho customer xem) - đơn giản
     */
    List<Coupon> getAvailableCouponsForExchange();
    
    /**
     * Đổi điểm lấy coupon
     */
    Coupon exchangePointsForCoupon(Long customerId, Long couponId);
    
    /**
     * Lấy thông tin coupon theo ID
     */
    Coupon getCouponById(Long couponId);
    
    /**
     * Lấy coupon theo code
     */
    Coupon getCouponByCode(String code);
    
    /**
     * ADMIN: Lấy tất cả coupons
     */
    List<Coupon> getAllCoupons();
    
    /**
     * ADMIN: Tạo coupon mới
     */
    Coupon createCoupon(Coupon coupon);
    
    /**
     * ADMIN: Cập nhật coupon
     */
    Coupon updateCoupon(Long couponId, Coupon coupon);
    
    /**
     * ADMIN: Xóa coupon
     */
    void deleteCoupon(Long couponId);
}
