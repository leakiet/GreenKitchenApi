package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.CartEmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CartEmailLogRepository extends JpaRepository<CartEmailLog, Long> {
    
    // Kiểm tra khách hàng đã nhận email cart abandonment trong X ngày gần đây
    @Query("SELECT COUNT(c) > 0 FROM CartEmailLog c WHERE c.customerId = :customerId AND c.emailType = 'CART_ABANDONMENT' AND c.emailSentAt >= :since")
    boolean hasReceivedCartEmailRecently(@Param("customerId") Long customerId, @Param("since") LocalDateTime since);
    
    // Lấy lần gửi email cart abandonment gần nhất
    @Query("SELECT c FROM CartEmailLog c WHERE c.customerId = :customerId AND c.emailType = 'CART_ABANDONMENT' ORDER BY c.emailSentAt DESC")
    List<CartEmailLog> findLatestCartEmailsByCustomer(@Param("customerId") Long customerId);
    
    // Đếm số email đã gửi cho khách hàng
    @Query("SELECT COUNT(c) FROM CartEmailLog c WHERE c.customerId = :customerId AND c.emailType = 'CART_ABANDONMENT'")
    long countCartEmailsByCustomer(@Param("customerId") Long customerId);
    
    // Lấy khách hàng chưa nhận email trong X ngày
    @Query("SELECT DISTINCT c.customerId FROM CartEmailLog c WHERE c.emailType = 'CART_ABANDONMENT' AND c.emailSentAt < :since")
    List<Long> findCustomersEligibleForReminder(@Param("since") LocalDateTime since);
    
    // Lấy email logs theo customer ID với phân trang
    Page<CartEmailLog> findByCustomerId(Long customerId, Pageable pageable);
    
    // Đếm email theo type
    long countByEmailType(String emailType);
}
