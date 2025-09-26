package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;

public class CartEmailLogResponse {
    private Long id;
    private Long customerId;
    private String customerEmail;
    private LocalDateTime emailSentAt;
    private Integer cartItemsCount;
    private Double totalAmount;
    private String emailType;
    private String emailStatus;

    public CartEmailLogResponse() {}

    public CartEmailLogResponse(Long id,
                                Long customerId,
                                String customerEmail,
                                LocalDateTime emailSentAt,
                                Integer cartItemsCount,
                                Double totalAmount,
                                String emailType,
                                String emailStatus) {
        this.id = id;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.emailSentAt = emailSentAt;
        this.cartItemsCount = cartItemsCount;
        this.totalAmount = totalAmount;
        this.emailType = emailType;
        this.emailStatus = emailStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

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


