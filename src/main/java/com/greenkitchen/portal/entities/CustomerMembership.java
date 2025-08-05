package com.greenkitchen.portal.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.greenkitchen.portal.enums.MembershipTier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "customer_memberships")
public class CustomerMembership extends AbstractEntity {
    
    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    @JsonBackReference
    private Customer customer;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    private MembershipTier currentTier = MembershipTier.ENERGY; // Hạng hiện tại
    
    @Column(precision = 15, scale = 2)
    private BigDecimal totalSpentLast6Months = BigDecimal.ZERO; // Tổng chi tiêu 6 tháng qua
    
    @Column(precision = 10, scale = 2)
    private BigDecimal availablePoints = BigDecimal.ZERO; // Điểm còn lại (chưa hết hạn)
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPointsEarned = BigDecimal.ZERO; // Tổng điểm đã nhận
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPointsUsed = BigDecimal.ZERO; // Tổng điểm đã sử dụng
    
    private LocalDateTime lastUpdatedAt; // Lần cập nhật cuối
    
    private LocalDateTime tierAchievedAt; // Thời gian đạt hạng hiện tại
    
    public CustomerMembership(Customer customer) {
        this.customer = customer;
        this.currentTier = MembershipTier.ENERGY;
        this.totalSpentLast6Months = BigDecimal.ZERO;
        this.availablePoints = BigDecimal.ZERO;
        this.totalPointsEarned = BigDecimal.ZERO;
        this.totalPointsUsed = BigDecimal.ZERO;
        this.lastUpdatedAt = LocalDateTime.now();
        this.tierAchievedAt = LocalDateTime.now();
    }
}
