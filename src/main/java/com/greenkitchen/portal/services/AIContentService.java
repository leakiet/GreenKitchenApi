package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.AIContentRequest;
import com.greenkitchen.portal.dtos.AIContentResponse;

public interface AIContentService {
    AIContentResponse generatePostContent(AIContentRequest request);
    AIContentResponse generateTitleOnly(AIContentRequest request);
    AIContentResponse generateContentOnly(AIContentRequest request);
}
