package com.greenkitchen.portal.services;

import java.math.BigDecimal;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerMembership;
import com.greenkitchen.portal.enums.MembershipTier;

public interface MembershipService {
    
    /**
     * Tính toán và cập nhật membership khi customer có giao dịch mới
     */
    CustomerMembership updateMembershipAfterPurchase(Customer customer, BigDecimal spentAmount, String orderId);
    
    /**
     * Lấy thông tin membership hiện tại của customer
     */
    CustomerMembership getCurrentMembership(Customer customer);
    
    /**
     * Sử dụng điểm thưởng
     */
    boolean usePoints(Customer customer, BigDecimal pointsToUse, String description);
    
    /**
     * Lấy hạng membership hiện tại
     */
    MembershipTier getCustomerTier(Customer customer);
}
