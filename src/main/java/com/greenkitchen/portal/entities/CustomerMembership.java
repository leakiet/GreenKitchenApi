package com.greenkitchen.portal.entities;

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
    
    private Double totalSpentLast6Months = 0.0; // Tổng chi tiêu 6 tháng qua
    
    private Double availablePoints = 0.0; // Điểm còn lại (chưa hết hạn)
    
    private Double totalPointsEarned = 0.0; // Tổng điểm đã nhận
    
    private Double totalPointsUsed = 0.0; // Tổng điểm đã sử dụng
    
    private LocalDateTime lastUpdatedAt; // Lần cập nhật cuối
    
    private LocalDateTime tierAchievedAt; // Thời gian đạt hạng hiện tại
    
    public CustomerMembership(Customer customer) {
        this.customer = customer;
        this.currentTier = MembershipTier.ENERGY;
        this.totalSpentLast6Months = 0.0;
        this.availablePoints = 0.0;
        this.totalPointsEarned = 0.0;
        this.totalPointsUsed = 0.0;
        this.lastUpdatedAt = LocalDateTime.now();
        this.tierAchievedAt = LocalDateTime.now();
    }
}
