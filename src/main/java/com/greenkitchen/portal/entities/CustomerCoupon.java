package com.greenkitchen.portal.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.greenkitchen.portal.enums.CouponType;
import com.greenkitchen.portal.enums.CustomerCouponStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "customer_coupons")
public class CustomerCoupon extends AbstractEntity {
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    @JsonBackReference
    private Coupon coupon;
    
    @NotNull
    private LocalDateTime exchangedAt; // Thời gian đổi coupon
    
    @NotNull
    private LocalDateTime expiresAt; // Thời gian hết hạn (copy từ coupon hoặc có thể tính riêng)
    
    private LocalDateTime usedAt; // Thời gian sử dụng (null nếu chưa dùng)
    
    private Long orderId; // ID đơn hàng đã sử dụng coupon (nếu có)
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private CustomerCouponStatus status = CustomerCouponStatus.AVAILABLE;
    
    // Snapshot thông tin coupon tại thời điểm đổi (để đảm bảo dữ liệu không thay đổi)
    @NotBlank
    private String couponCode; // Mã coupon
    
    @NotBlank  
    private String couponName; // Tên coupon
    
    private String couponDescription; // Mô tả coupon
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private CouponType couponType; // PERCENTAGE, FIXED_AMOUNT
    
    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal couponDiscountValue; // Giá trị giảm giá
    
}
