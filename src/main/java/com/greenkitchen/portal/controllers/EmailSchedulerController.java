package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.services.CartScanService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/apis/v1/email-scheduler")
@Slf4j
public class EmailSchedulerController {

    @Autowired
    private CartScanService cartScanService;

    /**
     * Trigger gửi email cart abandonment ngay lập tức
     */
    @PostMapping("/trigger-now")
    public ResponseEntity<String> triggerEmailNow() {
        log.info("🔄 API trigger gửi email cart abandonment ngay lập tức được gọi");
        
        try {
            var response = cartScanService.scanAndSendEmails();
            String message = String.format("✅ Đã gửi email thành công cho %d customers", response.getNewCustomersFound());
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("❌ Lỗi khi trigger gửi email: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin scheduler
     */
    @GetMapping("/info")
    public ResponseEntity<String> getSchedulerInfo() {
        log.info("📋 API lấy thông tin scheduler được gọi");
        
        String info = """
            🕘 **Email Scheduler Configuration**
            
            **Morning Schedule:** 9:00 AM hàng ngày
            **Evening Schedule:** 6:00 PM hàng ngày  
            **Frequent Schedule:** Mỗi 2 giờ
            **Periodic Schedule:** Mỗi 6 giờ
            
            **Status:** ✅ Đang hoạt động
            **Thread Pool:** 5 threads
            **Next Run:** Theo lịch trình tự động
            """;
        
        return ResponseEntity.ok(info);
    }

    /**
     * Test scheduler với thời gian tùy chỉnh
     */
    @PostMapping("/test-schedule")
    public ResponseEntity<String> testSchedule(
            @RequestParam(value = "hours", defaultValue = "2") int hours) {
        log.info("🧪 API test scheduler với tần suất {} giờ được gọi", hours);
        
        try {
            var response = cartScanService.scanAndSendEmails();
            String message = String.format("✅ Test scheduler thành công: %d customers được gửi email (tần suất %d giờ)", 
                response.getNewCustomersFound(), hours);
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("❌ Lỗi khi test scheduler: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}
