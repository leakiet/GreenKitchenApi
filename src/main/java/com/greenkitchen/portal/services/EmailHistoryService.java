package com.greenkitchen.portal.services;

import com.greenkitchen.portal.entities.EmailHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EmailHistoryService {
    
    // Lưu lịch sử email
    EmailHistory saveEmailHistory(EmailHistory emailHistory);
    
    // Lấy lịch sử email với phân trang
    Page<EmailHistory> getEmailHistory(Pageable pageable);
    
    // Lấy lịch sử email theo thời gian
    Page<EmailHistory> getEmailHistoryByDateRange(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    // Lấy lịch sử email theo loại
    Page<EmailHistory> getEmailHistoryByType(String emailType, Pageable pageable);
    
    // Lấy lịch sử email gần đây (30 ngày)
    List<EmailHistory> getRecentEmailHistory();
    
    // Thống kê email
    Map<String, Object> getEmailStatistics();
    
    // Lấy chi tiết email
    EmailHistory getEmailHistoryById(Long id);
}
