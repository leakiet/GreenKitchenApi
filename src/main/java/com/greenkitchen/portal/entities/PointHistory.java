package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.greenkitchen.portal.enums.PointTransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "point_histories")
public class PointHistory extends AbstractEntity {
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private Customer customer;
    
    @NotNull
    private Double spentAmount; // Số tiền đã chi tiêu
    
    @NotNull
    private Double pointsEarned; // Điểm thưởng nhận được
    
    @NotNull
    private LocalDateTime earnedAt; // Thời gian nhận điểm
    
    @NotNull
    private LocalDateTime expiresAt; // Thời gian hết hạn (6 tháng từ earnedAt)

    @Enumerated(EnumType.STRING)
    private PointTransactionType transactionType = PointTransactionType.EARNED; // EARNED, USED, EXPIRED
    
    private String description; // Mô tả giao dịch
    
    private String orderId; // ID đơn hàng (nếu có)
    
    private Boolean isExpired = false; // Đã hết hạn chưa
    
    public PointHistory(Customer customer, Double spentAmount, Double pointsEarned, String description, String orderId) {
        this.customer = customer;
        this.spentAmount = spentAmount;
        this.pointsEarned = pointsEarned;
        this.earnedAt = LocalDateTime.now();
        this.expiresAt = this.earnedAt.plusMonths(6);
        this.transactionType = PointTransactionType.EARNED;
        this.description = description;
        this.orderId = orderId;
        this.isExpired = false;
    }
}
