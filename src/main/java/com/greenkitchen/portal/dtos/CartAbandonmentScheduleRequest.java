package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class CartAbandonmentScheduleRequest {
    @NotBlank(message = "Tên lịch không được để trống")
    private String scheduleName;

    @NotNull(message = "Thời gian hàng ngày không được để trống")
    private LocalTime dailyTime;

    private Integer intervalHours;

    @NotNull(message = "Trạng thái kích hoạt hàng ngày không được để trống")
    private Boolean isDailyEnabled = true;

    @NotNull(message = "Trạng thái kích hoạt theo khoảng cách không được để trống")
    private Boolean isIntervalEnabled = false;

    @NotNull(message = "Trạng thái kích hoạt buổi tối không được để trống")
    private Boolean isEveningEnabled = false;

    private LocalTime eveningTime;

    private String description;

    // Constructors
    public CartAbandonmentScheduleRequest() {}

    public CartAbandonmentScheduleRequest(String scheduleName, LocalTime dailyTime) {
        this.scheduleName = scheduleName;
        this.dailyTime = dailyTime;
    }

    // Getters and Setters
    public String getScheduleName() { return scheduleName; }
    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }

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
}
