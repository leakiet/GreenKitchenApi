package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.entities.EmailTracking;
import com.greenkitchen.portal.repositories.EmailTrackingRepository;
import com.greenkitchen.portal.services.EmailTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class EmailTrackingServiceImpl implements EmailTrackingService {
    
    @Autowired
    private EmailTrackingRepository emailTrackingRepository;
    
    @Value("${app.backend.url")
    private String backendUrl;
    
    @Override
    public String createTrackingUrl(String originalUrl, String linkType, Long customerId, String customerEmail, String emailType) {
        // Tạo unique tracking ID
        String trackingId = UUID.randomUUID().toString().replace("-", "");
        
        // Lưu tracking record vào database
        EmailTracking tracking = new EmailTracking();
        tracking.setTrackingId(trackingId);
        tracking.setCustomerId(customerId);
        tracking.setCustomerEmail(customerEmail);
        tracking.setEmailType(emailType);
        tracking.setOriginalUrl(originalUrl);
        tracking.setLinkType(linkType);
        tracking.setCreatedAt(LocalDateTime.now());
        
        emailTrackingRepository.save(tracking);
        
        // Tạo tracking URL
        return String.format("%s/apis/v1/email-tracking/click/%s", backendUrl, trackingId);
    }
    
    @Override
    public void trackClick(String trackingId, String ipAddress, String userAgent) {
        EmailTracking tracking = emailTrackingRepository.findByTrackingId(trackingId).orElse(null);
        
        if (tracking != null && tracking.getClickedAt() == null) {
            tracking.setClickedAt(LocalDateTime.now());
            tracking.setIpAddress(ipAddress);
            tracking.setUserAgent(userAgent);
            emailTrackingRepository.save(tracking);
        }
    }
    
    @Override
    public Map<String, Object> getTrackingStats(String emailType) {
        Map<String, Object> stats = new HashMap<>();
        
        // Tổng số clicks
        Long totalClicks = emailTrackingRepository.countClicksByEmailType(emailType);
        stats.put("totalClicks", totalClicks);
        
        // Clicks trong 24h qua
        Long clicksLast24h = emailTrackingRepository.countClicksByEmailTypeSince(
            emailType, LocalDateTime.now().minusDays(1));
        stats.put("clicksLast24h", clicksLast24h);
        
        // Clicks trong 7 ngày qua
        Long clicksLast7Days = emailTrackingRepository.countClicksByEmailTypeSince(
            emailType, LocalDateTime.now().minusDays(7));
        stats.put("clicksLast7Days", clicksLast7Days);
        
        // Thống kê theo loại link
        List<Object[]> linkTypeStats = emailTrackingRepository.getClickStatsByLinkType(emailType);
        Map<String, Long> linkTypeMap = new HashMap<>();
        for (Object[] row : linkTypeStats) {
            linkTypeMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("clicksByLinkType", linkTypeMap);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getCustomerTrackingStats(Long customerId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Lấy tất cả tracking records của customer
        List<EmailTracking> customerTrackings = emailTrackingRepository.findByCustomerIdAndEmailType(customerId, "CART_ABANDONMENT");
        
        long totalClicks = customerTrackings.stream()
            .filter(t -> t.getClickedAt() != null)
            .count();
        
        stats.put("totalClicks", totalClicks);
        stats.put("totalEmails", customerTrackings.size());
        
        // Thống kê theo loại link
        Map<String, Long> linkTypeMap = new HashMap<>();
        for (EmailTracking tracking : customerTrackings) {
            if (tracking.getClickedAt() != null) {
                linkTypeMap.merge(tracking.getLinkType(), 1L, Long::sum);
            }
        }
        stats.put("clicksByLinkType", linkTypeMap);
        
        return stats;
    }
}
