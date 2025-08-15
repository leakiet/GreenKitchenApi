package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;

import com.greenkitchen.portal.enums.PaymentMethod;
import com.greenkitchen.portal.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "payments")
public class Payment extends AbstractEntity {
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @NotNull
    @Column(precision = 15)
    private Double amount; // Số tiền thanh toán
    
    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentMethod paymentMethod; // COD, PAYPAL
    
    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentStatus paymentStatus; // PENDING, COMPLETED, FAILED, CANCELLED
    
    @Column(length = 255)
    private String paymentReference; // PayPal Order ID, COD reference, etc.
    
    @Column(length = 255)
    private String paymentGatewayResponse; // Response từ payment gateway
    
    private LocalDateTime paidAt; // Thời gian thanh toán thành công
    
    @Column(length = 500)
    private String notes; // Ghi chú thanh toán
    
    // Constructor cho COD payment
    public Payment(Order order, Customer customer, Double amount, String notes, PaymentMethod method) {
        this.order = order;
        this.customer = customer;
        this.amount = amount;
        this.paymentMethod = method;
        this.paymentStatus = PaymentStatus.PENDING;
        this.notes = notes;
    }
    
    // Constructor cho PayPal payment
    public Payment(Order order, Customer customer, Double amount, String paypalOrderId, boolean isPaypal) {
        this.order = order;
        this.customer = customer;
        this.amount = amount;
        this.paymentMethod = PaymentMethod.PAYPAL;
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paymentReference = paypalOrderId;
        this.paidAt = LocalDateTime.now();
    }
    
    // Method để complete payment (cho COD)
    public void completePayment() {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }
    
    // Method để cancel payment
    public void cancelPayment(String reason) {
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.notes = (this.notes != null ? this.notes + " | " : "") + "Cancelled: " + reason;
    }
}
