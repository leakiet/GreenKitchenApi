package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.greenkitchen.portal.enums.CouponType;
import com.greenkitchen.portal.enums.CouponStatus;
import com.greenkitchen.portal.enums.CouponApplicability;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code; // Mã coupon (ví dụ: SAVE10, WELCOME2024)

    @NotBlank(message = "Coupon name is required")
    private String name; // Tên coupon hiển thị

    private String description; // Mô tả coupon

    @NotNull
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

    @NotNull
    private CouponStatus status = CouponStatus.ACTIVE; // ACTIVE, INACTIVE, EXPIRED

    @NotNull
    private CouponApplicability applicability = CouponApplicability.GENERAL; // GENERAL hoặc SPECIFIC_CUSTOMER

    private List<Long> customerIds; // Danh sách customer IDs (chỉ cần khi couponType = SPECIFIC_CUSTOMER)
}
