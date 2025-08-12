package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;

import com.greenkitchen.portal.enums.CustomerCouponStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UseCouponRequest {
    
    private Long id;
    
    private LocalDateTime usedAt;
    
    private Long orderId;
    
    private CustomerCouponStatus status;
}
