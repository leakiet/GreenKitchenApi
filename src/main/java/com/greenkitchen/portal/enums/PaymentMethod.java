package com.greenkitchen.portal.enums;

public enum PaymentMethod {
    COD("Cash on Delivery"),
    PAYPAL("PayPal Payment");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
