package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.CartScanResponse;

public interface CartScanService {
    
    /**
     * Quét tất cả customer có cart không rỗng (lần đầu)
     */
    CartScanResponse scanAllCustomersWithCarts();
    
    /**
     * Quét test - chỉ quét các customer chưa được quét
     */
    CartScanResponse testScanCustomersWithCarts();
    
    /**
     * Lấy danh sách customerId có cart không rỗng
     */
    java.util.List<Long> getCustomerIdsWithNonEmptyCarts();
    
    /**
     * Kiểm tra customer đã được quét chưa
     */
    boolean isCustomerAlreadyScanned(Long customerId);
    
    /**
     * Quét và gửi email cart abandonment
     */
    CartScanResponse scanAndSendEmails();
    
    /**
     * Gửi email test cho một customer cụ thể
     */
    void sendTestEmail(Long customerId, String email);
}
