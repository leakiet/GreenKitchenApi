package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotBlank;

public class BroadcastEmailRequest {
    @NotBlank
    private String subject;

    @NotBlank
    private String content;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}


