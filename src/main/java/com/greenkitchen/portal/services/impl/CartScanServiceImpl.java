package com.greenkitchen.portal.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.dtos.CartScanResponse;
import com.greenkitchen.portal.dtos.CartScanResponse.CustomerCartInfo;
import com.greenkitchen.portal.entities.Cart;
import com.greenkitchen.portal.entities.CartScanLog;
import com.greenkitchen.portal.repositories.CartRepository;
import com.greenkitchen.portal.repositories.CartScanLogRepository;
import com.greenkitchen.portal.services.CartScanService;
import com.greenkitchen.portal.services.CartEmailService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class CartScanServiceImpl implements CartScanService {

    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartScanLogRepository cartScanLogRepository;
    
    @Autowired
    private CartEmailService cartEmailService;
    
    @Autowired
    private com.greenkitchen.portal.repositories.CartEmailLogRepository cartEmailLogRepository;

    @Override
    public CartScanResponse scanAllCustomersWithCarts() {
        log.info("Bắt đầu quét tất cả customer có cart không rỗng");
        
        LocalDateTime scanStartedAt = LocalDateTime.now();
        List<Long> customerIdsWithCarts = getCustomerIdsWithNonEmptyCarts();
        
        List<CustomerCartInfo> customerCarts = new ArrayList<>();
        int newCustomersFound = 0;
        int existingCustomersSkipped = 0;
        
        for (Long customerId : customerIdsWithCarts) {
            // Kiểm tra cooldown: chỉ gửi email nếu chưa nhận trong 7 ngày
            if (!canReceiveCartEmail(customerId)) {
                existingCustomersSkipped++;
                log.debug("Customer {} đã nhận email cart abandonment trong 7 ngày gần đây, bỏ qua", customerId);
                continue;
            }
            
            // Lấy thông tin cart của customer
            Cart cart = cartRepository.findByCustomerIdWithActiveItems(customerId).orElse(null);
            if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                CustomerCartInfo cartInfo = new CustomerCartInfo();
                cartInfo.setCustomerId(customerId);
                cartInfo.setCartItemsCount(cart.getCartItems().size());
                cartInfo.setTotalAmount(cart.getTotalAmount());
                cartInfo.setScanType("INITIAL");
                cartInfo.setScannedAt(LocalDateTime.now());
                
                customerCarts.add(cartInfo);
                
                // Lưu log quét
                CartScanLog scanLog = new CartScanLog(
                    customerId, 
                    cart.getCartItems().size(), 
                    cart.getTotalAmount(), 
                    "INITIAL"
                );
                cartScanLogRepository.save(scanLog);
                
                newCustomersFound++;
                log.info("Đã quét customer {} với {} items, tổng tiền: {}", 
                    customerId, cart.getCartItems().size(), cart.getTotalAmount());
            }
        }
        
        LocalDateTime scanCompletedAt = LocalDateTime.now();
        
        CartScanResponse response = new CartScanResponse();
        response.setTotalCustomersScanned(customerIdsWithCarts.size());
        response.setNewCustomersFound(newCustomersFound);
        response.setExistingCustomersSkipped(existingCustomersSkipped);
        response.setScanStartedAt(scanStartedAt);
        response.setScanCompletedAt(scanCompletedAt);
        response.setCustomerCarts(customerCarts);
        
        log.info("Hoàn thành quét cart: {} customers được quét, {} mới, {} đã có", 
            response.getTotalCustomersScanned(), newCustomersFound, existingCustomersSkipped);
        
        return response;
    }

    @Override
    public CartScanResponse testScanCustomersWithCarts() {
        log.info("Bắt đầu quét test - chỉ quét customer chưa được quét");
        
        LocalDateTime scanStartedAt = LocalDateTime.now();
        List<Long> customerIdsWithCarts = getCustomerIdsWithNonEmptyCarts();
        
        // Lọc ra các customer chưa được quét
        List<Long> unscannedCustomerIds = customerIdsWithCarts.stream()
            .filter(customerId -> !isCustomerAlreadyScanned(customerId))
            .collect(Collectors.toList());
        
        List<CustomerCartInfo> customerCarts = new ArrayList<>();
        int newCustomersFound = 0;
        
        for (Long customerId : unscannedCustomerIds) {
            // Lấy thông tin cart của customer
            Cart cart = cartRepository.findByCustomerIdWithActiveItems(customerId).orElse(null);
            if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                CustomerCartInfo cartInfo = new CustomerCartInfo();
                cartInfo.setCustomerId(customerId);
                cartInfo.setCartItemsCount(cart.getCartItems().size());
                cartInfo.setTotalAmount(cart.getTotalAmount());
                cartInfo.setScanType("TEST");
                cartInfo.setScannedAt(LocalDateTime.now());
                
                customerCarts.add(cartInfo);
                
                // Lưu log quét với scan type là TEST
                CartScanLog scanLog = new CartScanLog(
                    customerId, 
                    cart.getCartItems().size(), 
                    cart.getTotalAmount(), 
                    "TEST"
                );
                cartScanLogRepository.save(scanLog);
                
                newCustomersFound++;
                log.info("TEST: Đã quét customer {} với {} items, tổng tiền: {}", 
                    customerId, cart.getCartItems().size(), cart.getTotalAmount());
            }
        }
        
        LocalDateTime scanCompletedAt = LocalDateTime.now();
        
        CartScanResponse response = new CartScanResponse();
        response.setTotalCustomersScanned(unscannedCustomerIds.size());
        response.setNewCustomersFound(newCustomersFound);
        response.setExistingCustomersSkipped(customerIdsWithCarts.size() - unscannedCustomerIds.size());
        response.setScanStartedAt(scanStartedAt);
        response.setScanCompletedAt(scanCompletedAt);
        response.setCustomerCarts(customerCarts);
        
        log.info("Hoàn thành quét test: {} customers được quét, {} mới", 
            response.getTotalCustomersScanned(), newCustomersFound);
        
        return response;
    }

    @Override
    public List<Long> getCustomerIdsWithNonEmptyCarts() {
        // Sử dụng CartRepository có sẵn để lấy customerId có cart không rỗng
        List<Long> customerIds = new ArrayList<>();
        
        // Lấy tất cả cart có items
        List<Cart> cartsWithItems = cartRepository.findAll().stream()
            .filter(cart -> cart.getCartItems() != null && !cart.getCartItems().isEmpty())
            .collect(Collectors.toList());
        
        for (Cart cart : cartsWithItems) {
            if (cart.getCustomerId() != null) {
                customerIds.add(cart.getCustomerId());
            }
        }
        
        log.debug("Tìm thấy {} customers có cart không rỗng", customerIds.size());
        return customerIds;
    }

    @Override
    public boolean isCustomerAlreadyScanned(Long customerId) {
        return cartScanLogRepository.existsByCustomerId(customerId);
    }
    
    // Kiểm tra khách hàng có thể nhận email cart abandonment (cooldown 7 ngày)
    public boolean canReceiveCartEmail(Long customerId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return !cartEmailLogRepository.hasReceivedCartEmailRecently(customerId, sevenDaysAgo);
    }
    
    // Kiểm tra khách hàng có thể nhận email nhắc nhở (cooldown 14 ngày)
    public boolean canReceiveReminderEmail(Long customerId) {
        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);
        return !cartEmailLogRepository.hasReceivedCartEmailRecently(customerId, fourteenDaysAgo);
    }
    
    @Override
    public CartScanResponse scanAndSendEmails() {
        log.info("Bắt đầu quét và gửi email cart abandonment");
        
        // Quét customers có cart
        CartScanResponse scanResponse = testScanCustomersWithCarts();
        
        if (scanResponse.getNewCustomersFound() > 0) {
            // Gửi email cho tất cả customers mới được quét
            cartEmailService.sendBulkCartAbandonmentEmails(scanResponse);
            log.info("Đã gửi email cart abandonment cho {} customers", scanResponse.getNewCustomersFound());
        } else {
            log.info("Không có customer mới để gửi email");
        }
        
        return scanResponse;
    }
    
    @Override
    public void sendTestEmail(Long customerId, String email) {
        log.info("Gửi email test cart cho customer ID: {} với email: {}", customerId, email);
        cartEmailService.sendTestCartEmail(customerId, email);
    }
}
