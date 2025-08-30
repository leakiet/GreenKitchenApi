package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart_scan_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartScanLog extends AbstractEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "last_scanned_at", nullable = false)
    private LocalDateTime lastScannedAt;

    @Column(name = "cart_items_count")
    private Integer cartItemsCount;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "scan_type")
    private String scanType; // "INITIAL", "TEST", "SCHEDULED"

    public CartScanLog(Long customerId, Integer cartItemsCount, Double totalAmount, String scanType) {
        this.customerId = customerId;
        this.cartItemsCount = cartItemsCount;
        this.totalAmount = totalAmount;
        this.scanType = scanType;
        this.lastScannedAt = LocalDateTime.now();
    }
}
