package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;

public interface ChatCommandService {
    ChatResponse sendMessage(Long actorId, ChatRequest request);
    void markCustomerMessagesAsRead(Long conversationId);
    void claimConversationAsEmp(Long conversationId, Long employeeId);
    void releaseConversationToAI(Long conversationId);
}
