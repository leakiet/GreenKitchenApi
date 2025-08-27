package com.greenkitchen.portal.dtos;

import com.greenkitchen.portal.enums.OrderStatus;
import com.greenkitchen.portal.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import com.greenkitchen.portal.entities.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Long customerId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String recipientName;
    private String recipientPhone;
    private LocalDateTime deliveryTime;

    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime shippingAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime canceledAt;

    private Double subtotal;
    private Double shippingFee;
    private Double membershipDiscount;
    private Double couponDiscount;
    private Double totalAmount;
    private Double pointEarn;
    private String notes;

    private String paymentMethod;
    private String paypalOrderId;

    private List<OrderItem> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
