package com.greenkitchen.portal.services;

import com.greenkitchen.portal.entities.EmailHistory;

public interface EmailSchedulerService {
    
    // Kiểm tra và gửi email đã lên lịch
    void processScheduledEmails();
    
    // Gửi email đã lên lịch
    void sendScheduledEmail(EmailHistory emailHistory);
    
    // Lấy danh sách email cần gửi
    java.util.List<EmailHistory> getEmailsToSend();
}
