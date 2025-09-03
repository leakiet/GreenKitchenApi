package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.entities.EmailHistory;
import com.greenkitchen.portal.repositories.EmailHistoryRepository;
import com.greenkitchen.portal.services.EmailHistoryService;
import com.greenkitchen.portal.services.EmailSchedulerService;
import com.greenkitchen.portal.services.MarketingEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailSchedulerServiceImpl implements EmailSchedulerService {

    @Autowired
    private EmailHistoryRepository emailHistoryRepository;
    
    @Autowired
    private EmailHistoryService emailHistoryService;
    
    @Autowired
    private MarketingEmailService marketingEmailService;

    // Chạy mỗi phút để kiểm tra email cần gửi
    @Scheduled(fixedRate = 60000) // 60 giây
    public void processScheduledEmails() {
        List<EmailHistory> emailsToSend = getEmailsToSend();
        
        for (EmailHistory email : emailsToSend) {
            try {
                sendScheduledEmail(email);
            } catch (Exception e) {
                // Cập nhật trạng thái thất bại
                email.setStatus("failed");
                emailHistoryService.saveEmailHistory(email);
                System.err.println("Failed to send scheduled email: " + email.getId() + " - " + e.getMessage());
            }
        }
    }

    @Override
    public List<EmailHistory> getEmailsToSend() {
        LocalDateTime now = LocalDateTime.now();
        // Lấy email có status = "scheduled" và scheduledAt <= now
        return emailHistoryRepository.findByStatusAndScheduledAtLessThanEqual("scheduled", now);
    }

    @Override
    public void sendScheduledEmail(EmailHistory emailHistory) {
        // Gửi email
        int sent = marketingEmailService.sendBroadcast(emailHistory.getSubject(), emailHistory.getContent());
        
        // Cập nhật trạng thái thành công
        emailHistory.setStatus("sent");
        emailHistory.setTotalSent(sent);
        emailHistory.setSentAt(LocalDateTime.now());
        emailHistoryService.saveEmailHistory(emailHistory);
        
        System.out.println("Successfully sent scheduled email: " + emailHistory.getId() + " to " + sent + " recipients");
    }
}
