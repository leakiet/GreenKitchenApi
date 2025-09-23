package com.greenkitchen.portal.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AIContentResponse {
    private String title;
    private String content;
    private String slug;
    private String excerpt;
    private String status;
    private String message; // for error messages or additional info
    private String promptSource; // "markdown" | "manual"
    private String imageInjection; // "ai" | "fallback" | "none"
}
