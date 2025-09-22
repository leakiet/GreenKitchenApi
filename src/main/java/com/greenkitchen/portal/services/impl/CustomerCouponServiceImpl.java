package com.greenkitchen.portal.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.dtos.CustomerCouponIdsResponse;
import com.greenkitchen.portal.dtos.UseCouponRequest;
import com.greenkitchen.portal.entities.CustomerCoupon;
import com.greenkitchen.portal.enums.CouponApplicability;
import com.greenkitchen.portal.enums.CustomerCouponStatus;
import com.greenkitchen.portal.repositories.CustomerCouponRepository;
import com.greenkitchen.portal.services.CustomerCouponService;

@Service
public class CustomerCouponServiceImpl implements CustomerCouponService {

    @Autowired
    private CustomerCouponRepository customerCouponRepository;

    @Override
    @Transactional
    public CustomerCoupon customerUsedCoupon(UseCouponRequest useCouponRequest) {
        // Tìm customer coupon theo ID
        CustomerCoupon customerCoupon = customerCouponRepository
            .findById(useCouponRequest.getId())
            .orElseThrow(() -> new IllegalArgumentException("Customer coupon not found"));
        
        // Kiểm tra coupon có thể sử dụng không
        if (customerCoupon.getStatus() != CustomerCouponStatus.AVAILABLE) {
            throw new IllegalStateException("Coupon không thể sử dụng");
        }
        
        if (customerCoupon.getUsedAt() != null) {
            throw new IllegalStateException("Coupon đã được sử dụng");
        }
        
        // Kiểm tra thời gian hết hạn  
        LocalDateTime now = LocalDateTime.now();
        if (customerCoupon.getExpiresAt().isBefore(now)) {
            throw new IllegalStateException("Coupon đã hết hạn");
        }
        
        // Cập nhật thông tin sử dụng coupon
        customerCoupon.setUsedAt(LocalDateTime.now());
        customerCoupon.setOrderId(useCouponRequest.getOrderId());
        customerCoupon.setStatus(CustomerCouponStatus.USED);
        customerCoupon.setUpdatedAt(LocalDateTime.now());
        
        return customerCouponRepository.save(customerCoupon);
    }

    @Override
    public CustomerCouponIdsResponse getCustomerIdsByCouponId(Long couponId) {
        List<Long> customerIds = customerCouponRepository.findCustomerIdsByCouponId(couponId);
        return new CustomerCouponIdsResponse(customerIds);
    }
    
    @Override
    public List<CustomerCoupon> getAvailableCustomerCoupons(Long customerId) {
        LocalDateTime now = LocalDateTime.now();
        return customerCouponRepository.findAvailableCustomerCoupons(
            customerId, 
            CustomerCouponStatus.AVAILABLE, 
            now,
            CouponApplicability.GENERAL
        );
    }
}
