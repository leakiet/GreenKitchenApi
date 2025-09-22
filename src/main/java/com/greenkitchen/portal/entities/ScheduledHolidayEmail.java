package com.greenkitchen.portal.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_holiday_emails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledHolidayEmail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "holiday_id", nullable = false)
    private Long holidayId;
    
    @Column(name = "holiday_name", nullable = false)
    private String holidayName;
    
    @Column(name = "holiday_date", nullable = false)
    private LocalDateTime holidayDate;
    
    @Column(name = "subject", nullable = false, length = 500)
    private String subject;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "schedule_at", nullable = false)
    private LocalDateTime scheduleAt;
    
    @Column(name = "target_audience", nullable = false)
    private String targetAudience = "all";
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "days_before", nullable = false)
    private Integer daysBefore = 1;
    
    @Column(name = "template_type")
    private String templateType = "generic";
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "sent_count")
    private Integer sentCount = 0;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailStatus status = EmailStatus.PENDING;
    
    public enum EmailStatus {
        PENDING,    // Chờ gửi
        SENT,       // Đã gửi
        FAILED,     // Gửi thất bại
        CANCELLED   // Đã hủy
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
