package com.greenkitchen.portal.dtos;

public class CapturePayPalOrderRequest {
    private String orderID;
    private String payerID;
    private String orderId;
    private Object paymentDetails; // Để nhận thông tin chi tiết từ frontend

    // Getters and setters
    public String getOrderID() { 
        return orderID; 
    }
    
    public void setOrderID(String orderID) { 
        this.orderID = orderID; 
    }
    
    public String getPayerID() { 
        return payerID; 
    }
    
    public void setPayerID(String payerID) { 
        this.payerID = payerID; 
    }
    
    public String getOrderId() { 
        return orderId; 
    }
    
    public void setOrderId(String orderId) { 
        this.orderId = orderId; 
    }
    
    public Object getPaymentDetails() { 
        return paymentDetails; 
    }
    
    public void setPaymentDetails(Object paymentDetails) { 
        this.paymentDetails = paymentDetails; 
    }
}
