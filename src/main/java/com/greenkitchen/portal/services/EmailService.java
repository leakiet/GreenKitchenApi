package com.greenkitchen.portal.services;

public interface EmailService {
    
    void sendVerificationEmail(String toEmail, String verifyToken);
    void sendOtpEmail(String toEmail, String otpCode);

    void sendFeedbackEmail(String fromEmail, String type, Integer rating, String title, String description, String contactEmail);

    void sendSupportRequestEmail(String issueType, String priority, String subject, String description, String contactMethod, String contactValue);

}
