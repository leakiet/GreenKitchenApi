package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.entities.EmailTracking;
import com.greenkitchen.portal.repositories.EmailTrackingRepository;
import com.greenkitchen.portal.services.EmailTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/apis/v1/email-tracking")
public class EmailTrackingController {
    
    @Autowired
    private EmailTrackingService emailTrackingService;
    
    @Autowired
    private EmailTrackingRepository emailTrackingRepository;
    
    /**
     * Xử lý khi user click vào tracking link
     * @param trackingId ID tracking duy nhất
     * @param request HttpServletRequest để lấy IP và User Agent
     * @return ResponseEntity với redirect đến URL gốc hoặc lỗi
     */
    @GetMapping("/click/{trackingId}")
    public ResponseEntity<?> trackClick(
            @PathVariable("trackingId") String trackingId, 
            HttpServletRequest request) {
        try {
            // Lấy thông tin tracking
            EmailTracking tracking = emailTrackingRepository.findByTrackingId(trackingId).orElse(null);
            if (tracking == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Lấy thông tin request
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            // Track click
            emailTrackingService.trackClick(trackingId, ipAddress, userAgent);
            
            // Redirect đến URL gốc
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", tracking.getOriginalUrl())
                .build();
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to track click: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thống kê tracking cho email type
     * @param emailType Loại email (CART_ABANDONMENT, BROADCAST, etc.)
     * @return ResponseEntity với thống kê tracking
     */
    @GetMapping("/stats/{emailType}")
    public ResponseEntity<?> getTrackingStats(@PathVariable("emailType") String emailType) {
        try {
            Map<String, Object> stats = emailTrackingService.getTrackingStats(emailType);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get tracking stats: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thống kê tracking cho customer
     * @param customerId ID của customer
     * @return ResponseEntity với thống kê tracking của customer
     */
    @GetMapping("/customer-stats/{customerId}")
    public ResponseEntity<?> getCustomerTrackingStats(@PathVariable("customerId") Long customerId) {
        try {
            Map<String, Object> stats = emailTrackingService.getCustomerTrackingStats(customerId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get customer tracking stats: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thông tin tracking record
     * @param trackingId ID tracking duy nhất
     * @return ResponseEntity với thông tin tracking record
     */
    @GetMapping("/info/{trackingId}")
    public ResponseEntity<?> getTrackingInfo(@PathVariable("trackingId") String trackingId) {
        try {
            EmailTracking tracking = emailTrackingRepository.findByTrackingId(trackingId).orElse(null);
            if (tracking == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get tracking info: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy IP address của client từ request
     * @param request HttpServletRequest chứa thông tin request
     * @return IP address của client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
