package com.greenkitchen.portal.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_email_logs")
public class CartEmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "email_sent_at", nullable = false)
    private LocalDateTime emailSentAt;

    @Column(name = "cart_items_count")
    private Integer cartItemsCount;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "email_type")
    private String emailType; // "CART_ABANDONMENT", "REMINDER", "FINAL"

    @Column(name = "email_status")
    private String emailStatus; // "SENT", "OPENED", "CLICKED", "CONVERTED"

    // Constructors
    public CartEmailLog() {}

    public CartEmailLog(Long customerId, Integer cartItemsCount, Double totalAmount, String emailType) {
        this.customerId = customerId;
        this.cartItemsCount = cartItemsCount;
        this.totalAmount = totalAmount;
        this.emailType = emailType;
        this.emailSentAt = LocalDateTime.now();
        this.emailStatus = "SENT";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public LocalDateTime getEmailSentAt() { return emailSentAt; }
    public void setEmailSentAt(LocalDateTime emailSentAt) { this.emailSentAt = emailSentAt; }

    public Integer getCartItemsCount() { return cartItemsCount; }
    public void setCartItemsCount(Integer cartItemsCount) { this.cartItemsCount = cartItemsCount; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getEmailType() { return emailType; }
    public void setEmailType(String emailType) { this.emailType = emailType; }

    public String getEmailStatus() { return emailStatus; }
    public void setEmailStatus(String emailStatus) { this.emailStatus = emailStatus; }
}
