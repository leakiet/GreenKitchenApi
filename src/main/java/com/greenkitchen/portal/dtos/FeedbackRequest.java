package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackRequest {
    @NotBlank
    private String type; // GENERAL, FOOD_QUALITY, SERVICE, DELIVERY, WEBSITE

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @Email
    private String contactEmail;

    @Email
    private String fromEmail;
}


