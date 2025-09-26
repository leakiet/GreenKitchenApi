package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.entities.CartEmailLog;
import com.greenkitchen.portal.entities.EmailTracking;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.dtos.CartEmailLogResponse;
import com.greenkitchen.portal.repositories.CartEmailLogRepository;
import com.greenkitchen.portal.repositories.EmailTrackingRepository;
import com.greenkitchen.portal.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/apis/v1/email-logs")
public class EmailLogsController {

    @Autowired
    private CartEmailLogRepository cartEmailLogRepository;
    
    @Autowired
    private EmailTrackingRepository emailTrackingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Lấy danh sách cart email logs với phân trang
     */
    @GetMapping("/cart-emails")
    public ResponseEntity<Map<String, Object>> getCartEmailLogs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "emailType", required = false) String emailType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("emailSentAt").descending());
            Page<CartEmailLog> emailLogs;
            
            if (customerId != null) {
                emailLogs = cartEmailLogRepository.findByCustomerId(customerId, pageable);
            } else {
                emailLogs = cartEmailLogRepository.findAll(pageable);
            }
            
            // Enrich with customerEmail
            List<CartEmailLogResponse> enriched = emailLogs.getContent().stream().map(log -> {
                String email = null;
                try {
                    if (log.getCustomerId() != null) {
                        Customer c = customerRepository.findById(log.getCustomerId()).orElse(null);
                        email = c != null ? c.getEmail() : null;
                    }
                } catch (Exception ignored) {}
                return new CartEmailLogResponse(
                        log.getId(),
                        log.getCustomerId(),
                        email,
                        log.getEmailSentAt(),
                        log.getCartItemsCount(),
                        log.getTotalAmount(),
                        log.getEmailType(),
                        log.getEmailStatus()
                );
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("emailLogs", enriched);
            response.put("totalElements", emailLogs.getTotalElements());
            response.put("totalPages", emailLogs.getTotalPages());
            response.put("currentPage", emailLogs.getNumber());
            response.put("size", emailLogs.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy email logs của một khách hàng cụ thể
     */
    @GetMapping("/cart-emails/customer/{customerId}")
    public ResponseEntity<List<CartEmailLogResponse>> getCartEmailLogsByCustomer(@PathVariable Long customerId) {
        try {
            List<CartEmailLog> logs = cartEmailLogRepository.findLatestCartEmailsByCustomer(customerId);
            final String email = customerRepository.findById(customerId)
                    .map(Customer::getEmail)
                    .orElse(null);
            List<CartEmailLogResponse> enriched = logs.stream().map(log -> new CartEmailLogResponse(
                    log.getId(),
                    log.getCustomerId(),
                    email,
                    log.getEmailSentAt(),
                    log.getCartItemsCount(),
                    log.getTotalAmount(),
                    log.getEmailType(),
                    log.getEmailStatus()
            )).toList();
            return ResponseEntity.ok(enriched);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Kiểm tra khách hàng đã nhận email cart abandonment gần đây không
     */
    @GetMapping("/cart-emails/customer/{customerId}/recent")
    public ResponseEntity<Map<String, Object>> checkRecentCartEmails(
            @PathVariable Long customerId,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            boolean hasRecentEmails = cartEmailLogRepository.hasReceivedCartEmailRecently(customerId, since);
            long totalEmails = cartEmailLogRepository.countCartEmailsByCustomer(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customerId);
            response.put("hasRecentEmails", hasRecentEmails);
            response.put("totalEmails", totalEmails);
            response.put("daysChecked", days);
            response.put("sinceDate", since);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy thống kê email logs
     */
    @GetMapping("/cart-emails/statistics")
    public ResponseEntity<Map<String, Object>> getEmailLogStatistics() {
        try {
            long totalEmails = cartEmailLogRepository.count();
            long cartAbandonmentEmails = cartEmailLogRepository.countByEmailType("CART_ABANDONMENT");
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalEmails", totalEmails);
            statistics.put("cartAbandonmentEmails", cartAbandonmentEmails);
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách email tracking với phân trang
     */
    @GetMapping("/tracking")
    public ResponseEntity<Map<String, Object>> getEmailTracking(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "emailType", required = false) String emailType) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<EmailTracking> trackingLogs;
            
            // Chỉ lấy các link có type "CART" và có clickedAt (tức là đã click)
            if (customerId != null) {
                trackingLogs = emailTrackingRepository.findByCustomerIdAndLinkTypeAndClickedAtIsNotNull(customerId, "CART", pageable);
            } else if (emailType != null) {
                trackingLogs = emailTrackingRepository.findByEmailTypeAndLinkTypeAndClickedAtIsNotNull(emailType, "CART", pageable);
            } else {
                trackingLogs = emailTrackingRepository.findByLinkTypeAndClickedAtIsNotNull("CART", pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("trackingLogs", trackingLogs.getContent());
            response.put("totalElements", trackingLogs.getTotalElements());
            response.put("totalPages", trackingLogs.getTotalPages());
            response.put("currentPage", trackingLogs.getNumber());
            response.put("size", trackingLogs.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy thống kê email tracking
     */
    @GetMapping("/tracking/statistics")
    public ResponseEntity<Map<String, Object>> getTrackingStatistics() {
        try {
            // Chỉ đếm CART clicks thực sự (clickedAt != null)
            long cartClicks = emailTrackingRepository.countByLinkTypeAndClickedAtIsNotNull("CART");
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalClicks", cartClicks);
            statistics.put("cartClicks", cartClicks);
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
