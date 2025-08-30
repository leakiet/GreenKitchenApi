package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.CartScanResponse;
import com.greenkitchen.portal.entities.Cart;

public interface CartEmailService {
    
    /**
     * Gửi email nhắc nhở cart abandonment cho một customer
     */
    void sendCartAbandonmentEmail(Cart cart, String customerEmail, String customerName);
    
    /**
     * Gửi email cho tất cả customers có cart bị bỏ quên
     */
    void sendBulkCartAbandonmentEmails(CartScanResponse scanResponse);
    
    /**
     * Gửi email test cho một customer cụ thể
     */
    void sendTestCartEmail(Long customerId, String customerEmail);
}
