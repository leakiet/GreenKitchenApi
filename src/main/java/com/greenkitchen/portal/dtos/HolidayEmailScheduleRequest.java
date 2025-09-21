package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class HolidayEmailScheduleRequest {
    
    @NotNull
    private Long holidayId;
    
    @NotNull
    private LocalDateTime scheduleAt;
    
    private String customSubject;
    private String customContent;
    private String targetAudience = "all"; // all, active, vip
    private boolean isActive = true;
    private int daysBefore = 1; // Send X days before holiday
    private String templateType = "generic"; // Template type for email

    public HolidayEmailScheduleRequest() {}

    public HolidayEmailScheduleRequest(Long holidayId, LocalDateTime scheduleAt) {
        this.holidayId = holidayId;
        this.scheduleAt = scheduleAt;
    }

    // Getters and Setters
    public Long getHolidayId() { return holidayId; }
    public void setHolidayId(Long holidayId) { this.holidayId = holidayId; }

    public LocalDateTime getScheduleAt() { return scheduleAt; }
    public void setScheduleAt(LocalDateTime scheduleAt) { this.scheduleAt = scheduleAt; }

    public String getCustomSubject() { return customSubject; }
    public void setCustomSubject(String customSubject) { this.customSubject = customSubject; }

    public String getCustomContent() { return customContent; }
    public void setCustomContent(String customContent) { this.customContent = customContent; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getDaysBefore() { return daysBefore; }
    public void setDaysBefore(int daysBefore) { this.daysBefore = daysBefore; }

    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }
}
