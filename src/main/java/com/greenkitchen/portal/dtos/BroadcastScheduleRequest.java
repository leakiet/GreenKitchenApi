package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BroadcastScheduleRequest {
    @NotBlank
    private String subject;

    @NotBlank
    private String content;

    @NotNull
    private java.time.LocalDateTime scheduleAt;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public java.time.LocalDateTime getScheduleAt() { return scheduleAt; }
    public void setScheduleAt(java.time.LocalDateTime scheduleAt) { this.scheduleAt = scheduleAt; }
}


