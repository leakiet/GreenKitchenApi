package com.greenkitchen.portal.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AIContentRequest {
    private String topic;
    private String category;
    private String style; // "formal", "casual", "professional", "friendly"
    private String targetAudience; // "general", "customers", "employees", "business"
    private Integer wordCount; // desired word count
    private String language; // "vi", "en"
    private String additionalInstructions; // any specific requirements
}
