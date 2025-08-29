package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.greenkitchen.portal.enums.CouponType;
import com.greenkitchen.portal.enums.CouponStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "coupons")
public class Coupon extends AbstractEntity {
    
    @NotBlank(message = "Coupon code is required")
    @Column(unique = true)
    private String code; // Mã coupon (ví dụ: SAVE10, WELCOME2024)
    
    @NotBlank(message = "Coupon name is required")
    private String name; // Tên coupon hiển thị
    
    private String description; // Mô tả coupon
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private CouponType type; // PERCENTAGE, FIXED_AMOUNT
    
    @NotNull
    @Min(value = 0, message = "Discount value must be positive")
    private Double discountValue; // Giá trị giảm giá 
    
    @Min(value = 0, message = "Max discount must be positive")
    private Double maxDiscount; // Giá trị giảm giá tối đa
    
    @NotNull
    @Min(value = 0, message = "Points required must be positive")
    private Double pointsRequired; // Số điểm cần thiết để đổi coupon
    
    @NotNull
    private LocalDateTime validUntil; // Thời gian hiệu lực kết thúc
    
    @Min(value = 0, message = "Exchange limit must be positive")
    private Integer exchangeLimit; // Số lượng coupon có thể đổi (null = không giới hạn)
    
    @Min(value = 0, message = "Exchange count cannot be negative")
    private Integer exchangeCount = 0; // Số lượng đã được đổi
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private CouponStatus status = CouponStatus.ACTIVE; // ACTIVE, INACTIVE, EXPIRED
    
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CustomerCoupon> customerCoupons;
    
}
