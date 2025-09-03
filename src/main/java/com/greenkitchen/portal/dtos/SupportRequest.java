package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportRequest {
    @NotBlank
    private String issueType; // TECHNICAL, ORDER, PAYMENT, DELIVERY, ACCOUNT

    @NotBlank
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    @NotBlank
    private String subject;

    @NotBlank
    private String description;

    @NotBlank
    private String contactMethod; // EMAIL, PHONE, CHAT

    @NotBlank
    private String contactValue; // actual email/phone/chat id
}


