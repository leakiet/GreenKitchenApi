package com.greenkitchen.portal.services;

public interface ChatSummaryService {

    String buildContextForAi(Long conversationId, String currentUserMessage);

    void summarizeIncrementally(Long conversationId);
}


