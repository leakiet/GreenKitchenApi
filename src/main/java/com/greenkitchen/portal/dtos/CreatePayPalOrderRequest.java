package com.greenkitchen.portal.dtos;

public class CreatePayPalOrderRequest {
    private String amount;
    private String currency;
    private String orderId;
    private String description;

    // Getters and setters
    public String getAmount() { 
        return amount; 
    }
    
    public void setAmount(String amount) { 
        this.amount = amount; 
    }
    
    public String getCurrency() { 
        return currency; 
    }
    
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }
    
    public String getOrderId() { 
        return orderId; 
    }
    
    public void setOrderId(String orderId) { 
        this.orderId = orderId; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
}
