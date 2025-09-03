package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.entities.EmailHistory;
import com.greenkitchen.portal.repositories.EmailHistoryRepository;
import com.greenkitchen.portal.services.EmailHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailHistoryServiceImpl implements EmailHistoryService {

    @Autowired
    private EmailHistoryRepository emailHistoryRepository;

    @Override
    public EmailHistory saveEmailHistory(EmailHistory emailHistory) {
        return emailHistoryRepository.save(emailHistory);
    }

    @Override
    public Page<EmailHistory> getEmailHistory(Pageable pageable) {
        return emailHistoryRepository.findAll(pageable);
    }

    @Override
    public Page<EmailHistory> getEmailHistoryByDateRange(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable) {
        return emailHistoryRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
            startDate, endDate, pageable);
    }

    @Override
    public Page<EmailHistory> getEmailHistoryByType(String emailType, Pageable pageable) {
        return emailHistoryRepository.findByEmailTypeOrderByCreatedAtDesc(emailType, pageable);
    }

    @Override
    public List<EmailHistory> getRecentEmailHistory() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return emailHistoryRepository.findRecentEmails(thirtyDaysAgo);
    }

    @Override
    public Map<String, Object> getEmailStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Thống kê theo loại email
        List<Object[]> typeStats = emailHistoryRepository.getEmailStatsByType();
        Map<String, Long> emailTypeCounts = new HashMap<>();
        for (Object[] stat : typeStats) {
            emailTypeCounts.put((String) stat[0], (Long) stat[1]);
        }
        stats.put("emailTypeCounts", emailTypeCounts);
        
        // Thống kê theo tháng (6 tháng gần đây)
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Object[]> monthlyStats = emailHistoryRepository.getEmailStatsByMonth(sixMonthsAgo);
        stats.put("monthlyStats", monthlyStats);
        
        // Tổng số email đã gửi
        long totalEmails = emailHistoryRepository.count();
        stats.put("totalEmails", totalEmails);
        
        // Email gần đây
        List<EmailHistory> recentEmails = getRecentEmailHistory();
        stats.put("recentEmails", recentEmails);
        
        return stats;
    }

    @Override
    public EmailHistory getEmailHistoryById(Long id) {
        return emailHistoryRepository.findById(id).orElse(null);
    }
}
