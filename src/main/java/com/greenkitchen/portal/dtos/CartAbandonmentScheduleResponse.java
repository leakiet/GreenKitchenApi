package com.greenkitchen.portal.dtos;

import java.time.LocalTime;
import java.time.LocalDateTime;

public class CartAbandonmentScheduleResponse {
    private Long id;
    private String scheduleName;
    private Boolean isActive;
    private LocalTime dailyTime;
    private Integer intervalHours;
    private Boolean isDailyEnabled;
    private Boolean isIntervalEnabled;
    private Boolean isEveningEnabled;
    private LocalTime eveningTime;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    // Constructors
    public CartAbandonmentScheduleResponse() {}

    public CartAbandonmentScheduleResponse(Long id, String scheduleName, Boolean isActive, LocalTime dailyTime) {
        this.id = id;
        this.scheduleName = scheduleName;
        this.isActive = isActive;
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
}
