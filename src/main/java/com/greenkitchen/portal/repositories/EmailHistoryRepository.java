package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.EmailHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailHistoryRepository extends JpaRepository<EmailHistory, Long> {
    
    // Lấy lịch sử email theo thời gian
    Page<EmailHistory> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    // Lấy lịch sử email theo loại
    Page<EmailHistory> findByEmailTypeOrderByCreatedAtDesc(String emailType, Pageable pageable);
    
    // Lấy lịch sử email theo trạng thái
    Page<EmailHistory> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    // Lấy lịch sử email gần đây (30 ngày)
    @Query("SELECT e FROM EmailHistory e WHERE e.createdAt >= :since ORDER BY e.createdAt DESC")
    List<EmailHistory> findRecentEmails(@Param("since") LocalDateTime since);
    
    // Thống kê email theo loại
    @Query("SELECT e.emailType, COUNT(e) FROM EmailHistory e GROUP BY e.emailType")
    List<Object[]> getEmailStatsByType();
    
    // Thống kê email theo tháng
    @Query("SELECT YEAR(e.createdAt), MONTH(e.createdAt), COUNT(e), SUM(e.totalSent) " +
           "FROM EmailHistory e WHERE e.createdAt >= :since " +
           "GROUP BY YEAR(e.createdAt), MONTH(e.createdAt) " +
           "ORDER BY YEAR(e.createdAt) DESC, MONTH(e.createdAt) DESC")
    List<Object[]> getEmailStatsByMonth(@Param("since") LocalDateTime since);
    
    // Lấy email cần gửi (đã lên lịch và đến giờ)
    List<EmailHistory> findByStatusAndScheduledAtLessThanEqual(String status, LocalDateTime scheduledAt);
}
