package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotBlank;

public class RecurringEmailScheduleRequest {
    @NotBlank
    public String name;
    @NotBlank
    public String frequency; // HOURLY, DAILY, WEEKLY
    public Integer minuteOfHour; // 0..59
    public Integer hourOfDay;    // 0..23
    public String dayOfWeek;     // MONDAY..SUNDAY
    public boolean active = true;
    @NotBlank
    public String subject;
    @NotBlank
    public String content;
}


