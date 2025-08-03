package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;

import com.greenkitchen.portal.enums.OrderStatus;
import com.greenkitchen.portal.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {
    
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    
    // Có thể cập nhật thông tin giao hàng
    private String street;
    private String ward;
    private String district;
    private String city;
    private String recipientName;
    private String recipientPhone;
    private LocalDateTime deliveryTime;
    
    // Cập nhật ghi chú
    private String notes;
}
