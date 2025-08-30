package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartScanResponse {
    
    private Integer totalCustomersScanned;
    private Integer newCustomersFound;
    private Integer existingCustomersSkipped;
    private LocalDateTime scanStartedAt;
    private LocalDateTime scanCompletedAt;
    private List<CustomerCartInfo> customerCarts;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerCartInfo {
        private Long customerId;
        private Integer cartItemsCount;
        private Double totalAmount;
        private String scanType;
        private LocalDateTime scannedAt;
    }
}
