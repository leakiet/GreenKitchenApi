package com.greenkitchen.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  private String frontendUrl = "http://127.0.0.1:5173";

  @Override
  @Async
  public void sendVerificationEmail(String toEmail, String verifyToken) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(toEmail);
    message.setSubject("Green Kitchen - Email Verification");

    String verifyUrl = frontendUrl + "/verify-email?email=" + toEmail + "&token=" + verifyToken;
    String body = String.format(
        """
        Hello,

        Thank you for registering with Green Kitchen!

        Please click the link below to verify your email address:
        %s

        This verification link will expire in 24 hours.

        If you didn't create an account, please ignore this email.

        Best regards,
        Green Kitchen Team
        """,
        
        verifyUrl
    );

    message.setText(body);
    mailSender.send(message);
  }

  @Override
  @Async
  public void sendOtpEmail(String toEmail, String otpCode) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(toEmail);
    message.setSubject("Green Kitchen - Password Reset OTP Code");

    String body = String.format(
        """
        Hello,

        You have requested to reset your password for your Green Kitchen account.

        Your OTP code is: %s

        This code will expire in 5 minutes.

        If you didn't request a password reset, please ignore this email or contact support if you're concerned about security.

        For security reasons, do not share this code with anyone.

        Best regards,
        Green Kitchen Team
        """,
        otpCode
    );

    message.setText(body);
    mailSender.send(message);
  }

  @Override
  @Async
  public void sendFeedbackEmail(String from, String type, Integer rating, String title, String description, String contactEmail) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(fromEmail);
    message.setSubject("[Feedback] " + title);
    String body = String.format(
        """
        New customer feedback received

        Type: %s
        Rating: %s
        Title: %s
        Description:
        %s

        From: %s
        Contact Email: %s
        """,
        type,
        rating == null ? "N/A" : rating.toString(),
        title,
        description,
        from,
        contactEmail == null ? "(not provided)" : contactEmail
    );
    message.setText(body);
    mailSender.send(message);
  }

  @Override
  @Async
  public void sendSupportRequestEmail(String issueType, String priority, String subject, String description, String contactMethod, String contactValue) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(fromEmail);
    message.setSubject("[Support] " + subject);
    String body = String.format(
        """
        New support request received

        Issue Type: %s
        Priority: %s
        Subject: %s
        Description:
        %s

        Preferred Contact Method: %s
        Contact: %s
        """,
        issueType,
        priority,
        subject,
        description,
        contactMethod,
        contactValue
    );
    message.setText(body);
    mailSender.send(message);
  }

  @Override
  @Async
  public void sendOrderCreatedEmail(String toEmail, String orderCode, Double totalAmount) {
    if (toEmail == null || toEmail.isEmpty()) return;
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(toEmail);
    message.setSubject("Green Kitchen - Order Created");

    String orderUrl = frontendUrl + "/tracking-order?orderCode=" + orderCode;
    String body = String.format(
        """
        Hello,

        Your order %s has been created successfully.
        Total: %s VND

        You can track your order here:
        %s

        Thank you for choosing Green Kitchen!
        """,
        orderCode,
        totalAmount == null ? "0" : String.format("%,.0f", totalAmount),
        orderUrl
    );

    message.setText(body);
    mailSender.send(message);
  }

}
