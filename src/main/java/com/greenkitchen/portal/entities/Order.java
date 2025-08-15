package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.greenkitchen.portal.enums.OrderStatus;
import com.greenkitchen.portal.enums.PaymentStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Thông tin giao hàng - sử dụng street thay vì deliveryAddress
    private String street;
    private String ward;
    private String district;
    private String city;
    private String recipientName;
    private String recipientPhone;
    private LocalDateTime deliveryTime;

    // Thông tin giá cả
    @Column(nullable = false)
    private Double subtotal = 0.0;

    private Double shippingFee = 0.0;
    
    private Double membershipDiscount = 0.0; // Giảm giá membership thay vì pointsUsed
    
    private Double couponDiscount = 0.0; // Giảm giá coupon
    
    @Column(nullable = false)
    private Double totalAmount = 0.0;
    
    // Điểm thưởng kiếm được từ đơn hàng này
    private Double pointEarn = 0.0;

    // Ghi chú
    private String notes;
    
    // Phương thức thanh toán
    @Column(nullable = false)
    private String paymentMethod;

    // PayPal Order ID cho thanh toán PayPal
    @Column(name = "paypal_order_id")
    @JsonIgnore
    private String paypalOrderId;
    
    // Quan hệ với OrderItem
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderItem> orderItems = new ArrayList<>();
}
