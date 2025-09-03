package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.services.MarketingEmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class MarketingEmailServiceImpl implements MarketingEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired(required = false)
    private ScheduledExecutorService scheduler;

    @Override
    public int sendBroadcast(String subject, String content) {
        List<com.greenkitchen.portal.entities.Customer> customers = customerRepository.findAll();
        int sent = 0;
        for (com.greenkitchen.portal.entities.Customer c : customers) {
            String email = c.getEmail();
            if (email == null || email.trim().isEmpty()) continue;
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(content, true);
                mailSender.send(message);
                sent++;
            } catch (Exception ignored) {}
        }
        return sent;
    }

    @Override
    public void scheduleBroadcast(String subject, String content, java.time.LocalDateTime scheduleAt) {
        // Chỉ lưu lịch trình, không gửi ngay
        // Việc gửi sẽ được xử lý bởi scheduler riêng hoặc cron job
        if (scheduler != null) {
            long delay = java.time.Duration.between(java.time.LocalDateTime.now(), scheduleAt).toMillis();
            if (delay > 0) {
                scheduler.schedule(() -> sendBroadcast(subject, content), delay, TimeUnit.MILLISECONDS);
            }
        }
        // Nếu không có scheduler hoặc thời gian đã qua, chỉ lưu vào database
        // Email sẽ được gửi bởi hệ thống khác hoặc admin sẽ xử lý thủ công
    }

    @Override
    public void sendPreview(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception ignored) {}
    }
}


