package com.greenkitchen.portal.services;

import java.util.Map;

public interface EmailTrackingService {
    
    /**
     * Tạo tracking URL cho link trong email
     */
    String createTrackingUrl(String originalUrl, String linkType, Long customerId, String customerEmail, String emailType);
    
    /**
     * Xử lý khi user click vào tracking link
     */
    void trackClick(String trackingId, String ipAddress, String userAgent);
    
    /**
     * Lấy thống kê tracking cho email type
     */
    Map<String, Object> getTrackingStats(String emailType);
    
    /**
     * Lấy thống kê tracking cho customer
     */
    Map<String, Object> getCustomerTrackingStats(Long customerId);
}
