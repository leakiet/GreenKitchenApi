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
}
