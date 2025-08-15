package com.greenkitchen.portal.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.Order;
import com.greenkitchen.portal.entities.Payment;
import com.greenkitchen.portal.enums.PaymentMethod;
import com.greenkitchen.portal.enums.PaymentStatus;
import com.greenkitchen.portal.repositories.PaymentRepository;
import com.greenkitchen.portal.services.PaymentService;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Override
    public Payment createCODPayment(Order order, Customer customer, Double amount, String notes) {
        Payment payment = new Payment(order, customer, amount, notes, PaymentMethod.COD);
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment createPayPalPayment(Order order, Customer customer, Double amount, String paypalOrderId) {
        Payment payment = new Payment(order, customer, amount, paypalOrderId, true);
        Payment savedPayment = paymentRepository.save(payment);
        
        System.out.println("PayPal payment created - ID: " + savedPayment.getId());
        
        return savedPayment;
    }
    
    @Override
    public Payment completeCODPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
            payment.completePayment();
            Payment savedPayment = paymentRepository.save(payment);
            
            System.out.println("COD payment completed - ID: " + savedPayment.getId());
            
            return savedPayment;
        }
        
        throw new RuntimeException("Payment is not in pending status");
    }
    
    @Override
    public Payment cancelPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        payment.cancelPayment(reason);
        return paymentRepository.save(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Payment findByOrder(Order order) {
        return paymentRepository.findByOrder(order)
            .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Payment findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Payment findByPaymentReference(String paymentReference) {
        return paymentRepository.findByPaymentReference(paymentReference)
            .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByCustomer(Customer customer) {
        return paymentRepository.findByCustomer(customer);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getTotalPaidAmountByCustomer(Long customerId) {
        return paymentRepository.getTotalPaidAmountByCustomerId(customerId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status);
    }
}
