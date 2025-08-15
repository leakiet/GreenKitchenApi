package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.Order;
import com.greenkitchen.portal.entities.Payment;
import com.greenkitchen.portal.enums.PaymentMethod;
import com.greenkitchen.portal.enums.PaymentStatus;

public interface PaymentService {
    
    /**
     * Tạo payment record cho COD
     */
    Payment createCODPayment(Order order, Customer customer, Double amount, String notes);
    
    /**
     * Tạo payment record cho PayPal
     */
    Payment createPayPalPayment(Order order, Customer customer, Double amount, String paypalOrderId);
    
    /**
     * Complete COD payment khi giao hàng thành công
     */
    Payment completeCODPayment(Long paymentId);
    
    /**
     * Cancel payment
     */
    Payment cancelPayment(Long paymentId, String reason);
    
    /**
     * Tìm payment theo order
     */
    Payment findByOrder(Order order);
    
    /**
     * Tìm payment theo order ID
     */
    Payment findByOrderId(Long orderId);
    
    /**
     * Tìm payment theo PayPal reference
     */
    Payment findByPaymentReference(String paymentReference);
    
    /**
     * Lấy danh sách payments của customer
     */
    List<Payment> getPaymentsByCustomer(Customer customer);
    
    /**
     * Lấy tổng số tiền đã thanh toán của customer
     */
    Double getTotalPaidAmountByCustomer(Long customerId);
    
    /**
     * Lấy payments theo status
     */
    List<Payment> getPaymentsByStatus(PaymentStatus status);
}
