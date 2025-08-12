package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.UseCouponRequest;
import com.greenkitchen.portal.entities.CustomerCoupon;

public interface CustomerCouponService {
    CustomerCoupon customerUsedCoupon(UseCouponRequest useCouponRequest);
}
