package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.CartScanResponse;
import com.greenkitchen.portal.services.CartScanService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/apis/v1/cart-scan")
@Slf4j
public class CartScanController {

    @Autowired
    private CartScanService cartScanService;

    /**
     * Endpoint test quét cart - chỉ quét các customer chưa được quét
     */
    @PostMapping("/test")
    public ResponseEntity<CartScanResponse> testScanCustomersWithCarts() {
        log.info("API test quét cart được gọi");
        
        try {
            CartScanResponse response = cartScanService.testScanCustomersWithCarts();
            log.info("Test quét cart thành công: {} customers mới được quét", response.getNewCustomersFound());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi test quét cart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint quét tất cả customer có cart (lần đầu)
     */
    @PostMapping("/scan-all")
    public ResponseEntity<CartScanResponse> scanAllCustomersWithCarts() {
        log.info("API quét tất cả customer có cart được gọi");
        
        try {
            CartScanResponse response = cartScanService.scanAllCustomersWithCarts();
            log.info("Quét tất cả cart thành công: {} customers mới được quét", response.getNewCustomersFound());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi quét tất cả cart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint lấy thống kê quét cart
     */
    @GetMapping("/stats")
    public ResponseEntity<CartScanResponse> getCartScanStats() {
        log.info("API lấy thống kê quét cart được gọi");
        
        try {
            // Gọi test scan để lấy thống kê hiện tại
            CartScanResponse response = cartScanService.testScanCustomersWithCarts();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê quét cart", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint quét và gửi email cart abandonment
     */
    @PostMapping("/scan-and-send-emails")
    public ResponseEntity<CartScanResponse> scanAndSendEmails() {
        log.info("API quét và gửi email cart abandonment được gọi");
        
        try {
            CartScanResponse response = cartScanService.scanAndSendEmails();
            log.info("Quét và gửi email thành công: {} customers được gửi email", response.getNewCustomersFound());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi quét và gửi email", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint gửi email test cho một customer cụ thể
     */
    @PostMapping("/send-test-email")
    public ResponseEntity<String> sendTestEmail(
            @RequestParam("customerId") Long customerId,
            @RequestParam("email") String email) {
        log.info("API gửi email test cart được gọi cho customer ID: {} với email: {}", customerId, email);
        
        try {
            cartScanService.sendTestEmail(customerId, email);
            return ResponseEntity.ok("Đã gửi email test thành công");
        } catch (Exception e) {
            log.error("Lỗi khi gửi email test", e);
            return ResponseEntity.internalServerError().body("Lỗi khi gửi email test: " + e.getMessage());
        }
    }
}
