package com.greenkitchen.portal.services;

import java.util.List;
import com.greenkitchen.portal.dtos.CustomerCouponIdsResponse;
import com.greenkitchen.portal.dtos.UseCouponRequest;
import com.greenkitchen.portal.entities.CustomerCoupon;

public interface CustomerCouponService {
    CustomerCoupon customerUsedCoupon(UseCouponRequest useCouponRequest);
    CustomerCouponIdsResponse getCustomerIdsByCouponId(Long couponId);
    
    /**
     * Lấy danh sách customer coupons có sẵn cho customer
     */
    List<CustomerCoupon> getAvailableCustomerCoupons(Long customerId);
}
