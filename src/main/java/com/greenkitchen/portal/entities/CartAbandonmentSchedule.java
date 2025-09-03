package com.greenkitchen.portal.entities;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_abandonment_schedules")
public class CartAbandonmentSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_name", nullable = false)
    private String scheduleName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "daily_time", nullable = false)
    private LocalTime dailyTime; // Thời gian gửi hàng ngày (VD: 09:00)

    @Column(name = "interval_hours")
    private Integer intervalHours; // Khoảng cách giữa các lần gửi (VD: 6 giờ)

    @Column(name = "is_daily_enabled", nullable = false)
    private Boolean isDailyEnabled = true; // Bật/tắt gửi hàng ngày

    @Column(name = "is_interval_enabled", nullable = false)
    private Boolean isIntervalEnabled = false; // Bật/tắt gửi theo khoảng cách

    @Column(name = "is_evening_enabled", nullable = false)
    private Boolean isEveningEnabled = false; // Bật/tắt gửi buổi tối

    @Column(name = "evening_time")
    private LocalTime eveningTime; // Thời gian gửi buổi tối (VD: 18:00)

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    // Constructors
    public CartAbandonmentSchedule() {
        this.createdAt = LocalDateTime.now();
    }

    public CartAbandonmentSchedule(String scheduleName, LocalTime dailyTime) {
        this();
        this.scheduleName = scheduleName;
        this.dailyTime = dailyTime;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getScheduleName() { return scheduleName; }
    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalTime getDailyTime() { return dailyTime; }
    public void setDailyTime(LocalTime dailyTime) { this.dailyTime = dailyTime; }

    public Integer getIntervalHours() { return intervalHours; }
    public void setIntervalHours(Integer intervalHours) { this.intervalHours = intervalHours; }

    public Boolean getIsDailyEnabled() { return isDailyEnabled; }
    public void setIsDailyEnabled(Boolean isDailyEnabled) { this.isDailyEnabled = isDailyEnabled; }

    public Boolean getIsIntervalEnabled() { return isIntervalEnabled; }
    public void setIsIntervalEnabled(Boolean isIntervalEnabled) { this.isIntervalEnabled = isIntervalEnabled; }

    public Boolean getIsEveningEnabled() { return isEveningEnabled; }
    public void setIsEveningEnabled(Boolean isEveningEnabled) { this.isEveningEnabled = isEveningEnabled; }

    public LocalTime getEveningTime() { return eveningTime; }
    public void setEveningTime(LocalTime eveningTime) { this.eveningTime = eveningTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
