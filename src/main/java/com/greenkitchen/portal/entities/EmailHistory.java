package com.greenkitchen.portal.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_history")
public class EmailHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String emailType; // broadcast, cart_abandonment, preview, etc.

    @Column(nullable = false)
    private String status; // sent, scheduled, failed

    @Column
    private Integer totalSent = 0;

    @Column
    private String recipientEmail; // null nếu là broadcast

    @Column
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private String createdBy; // admin username

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public EmailHistory() {}

    public EmailHistory(String subject, String content, String emailType, String status, String createdBy) {
        this.subject = subject;
        this.content = content;
        this.emailType = emailType;
        this.status = status;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getEmailType() { return emailType; }
    public void setEmailType(String emailType) { this.emailType = emailType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalSent() { return totalSent; }
    public void setTotalSent(Integer totalSent) { this.totalSent = totalSent; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
