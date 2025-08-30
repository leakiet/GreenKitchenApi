package com.greenkitchen.portal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.CartScanResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CartAbandonmentScheduler {

    @Autowired
    private CartScanService cartScanService;

    /**
     * Chạy mỗi ngày lúc 9:00 AM để gửi email cart abandonment
     */
    @Scheduled(cron = "0 0 9 * * ?") // Mỗi ngày lúc 9:00 AM
    public void sendDailyCartAbandonmentEmails() {
        log.info("🕘 Bắt đầu scheduled task gửi email cart abandonment hàng ngày (9:00 AM)");
        
        try {
            CartScanResponse response = cartScanService.scanAndSendEmails();
            log.info("✅ Scheduled task hàng ngày hoàn thành: {} customers được gửi email", response.getNewCustomersFound());
        } catch (Exception e) {
            log.error("❌ Lỗi trong scheduled task hàng ngày: {}", e.getMessage());
        }
    }

    /**
     * Chạy mỗi 6 giờ để gửi email cart abandonment
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 giờ
    public void sendPeriodicCartAbandonmentEmails() {
        log.info("⏰ Bắt đầu scheduled task gửi email cart abandonment định kỳ (6 giờ)");
        
        try {
            CartScanResponse response = cartScanService.scanAndSendEmails();
            log.info("✅ Scheduled task định kỳ hoàn thành: {} customers được gửi email", response.getNewCustomersFound());
        } catch (Exception e) {
            log.error("❌ Lỗi trong scheduled task định kỳ: {}", e.getMessage());
        }
    }
    
    /**
     * Chạy mỗi 2 giờ để gửi email cart abandonment (tần suất cao hơn)
     */
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // 2 giờ
    public void sendFrequentCartAbandonmentEmails() {
        log.info("⚡ Bắt đầu scheduled task gửi email cart abandonment thường xuyên (2 giờ)");
        
        try {
            CartScanResponse response = cartScanService.scanAndSendEmails();
            log.info("✅ Scheduled task thường xuyên hoàn thành: {} customers được gửi email", response.getNewCustomersFound());
        } catch (Exception e) {
            log.error("❌ Lỗi trong scheduled task thường xuyên: {}", e.getMessage());
        }
    }
    
    /**
     * Chạy mỗi ngày lúc 6:00 PM để gửi email cart abandonment (buổi tối)
     */
    @Scheduled(cron = "0 0 18 * * ?") // Mỗi ngày lúc 6:00 PM
    public void sendEveningCartAbandonmentEmails() {
        log.info("🌆 Bắt đầu scheduled task gửi email cart abandonment buổi tối (6:00 PM)");
        
        try {
            CartScanResponse response = cartScanService.scanAndSendEmails();
            log.info("✅ Scheduled task buổi tối hoàn thành: {} customers được gửi email", response.getNewCustomersFound());
        } catch (Exception e) {
            log.error("❌ Lỗi trong scheduled task buổi tối: {}", e.getMessage());
        }
    }
}
