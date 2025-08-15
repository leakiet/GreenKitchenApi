package com.greenkitchen.portal.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.Order;
import com.greenkitchen.portal.entities.Payment;
import com.greenkitchen.portal.enums.PaymentMethod;
import com.greenkitchen.portal.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Tìm payment theo order
    Optional<Payment> findByOrder(Order order);
    
    // Tìm payment theo order ID
    Optional<Payment> findByOrderId(Long orderId);
    
    // Tìm payment theo order ID và payment method
    Optional<Payment> findByOrderIdAndPaymentMethod(Long orderId, PaymentMethod paymentMethod);
    
    // Tìm payments theo customer
    List<Payment> findByCustomer(Customer customer);
    
    // Tìm payments theo customer ID
    List<Payment> findByCustomerId(Long customerId);
    
    // Tìm payment theo PayPal order ID
    Optional<Payment> findByPaymentReference(String paymentReference);
    
    // Tìm payments theo payment method
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    // Tìm payments theo payment status
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    
    // Tìm payments theo customer và status
    List<Payment> findByCustomerAndPaymentStatus(Customer customer, PaymentStatus paymentStatus);
    
    // Tìm tổng số tiền đã thanh toán của customer
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.customer.id = :customerId AND p.paymentStatus = 'COMPLETED'")
    Double getTotalPaidAmountByCustomerId(@Param("customerId") Long customerId);
    
    // Tìm payments thành công trong khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.customer.id = :customerId AND p.paymentStatus = 'COMPLETED' AND p.paidAt >= :fromDate")
    List<Payment> findCompletedPaymentsByCustomerIdAndDateAfter(@Param("customerId") Long customerId, @Param("fromDate") java.time.LocalDateTime fromDate);
}
