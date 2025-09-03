package com.greenkitchen.portal.services;

public interface MarketingEmailService {
    int sendBroadcast(String subject, String content);
    void scheduleBroadcast(String subject, String content, java.time.LocalDateTime scheduleAt);
    void sendPreview(String to, String subject, String content);
}


