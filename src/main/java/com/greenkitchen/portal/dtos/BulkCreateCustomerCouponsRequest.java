package com.greenkitchen.portal.dtos;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkCreateCustomerCouponsRequest {

    @NotNull(message = "Coupon ID is required")
    private Long couponId;

    @NotEmpty(message = "Customer IDs list cannot be empty")
    private List<Long> customerIds;
}
