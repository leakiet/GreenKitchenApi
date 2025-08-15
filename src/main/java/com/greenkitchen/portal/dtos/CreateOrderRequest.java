package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    private Long customerId;
    
    // Thông tin giao hàng
    private String street;
    private String ward;
    private String district;
    private String city;
    private String recipientName;
    private String recipientPhone;
    private LocalDateTime deliveryTime;
    private String paypalOrderId;

    // Thông tin giá cả (frontend tính toán)
    private Double subtotal;
    private Double shippingFee;
    private Double membershipDiscount;
    private Double couponDiscount;
    private Double totalAmount;
    
    // Ghi chú
    private String notes;
    
    // Phương thức thanh toán
    private String paymentMethod; // "COD" hoặc "CARD"
    
    // Danh sách món ăn
    private List<OrderItemRequest> orderItems;
}
