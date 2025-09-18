package com.greenkitchen.portal.services;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.ConversationResponse;
import com.greenkitchen.portal.enums.ConversationStatus;

public interface ChatService {
    ChatResponse sendMessage(Long actorId, ChatRequest request);
    List<ChatResponse> getMessagesByConversation(Long conversationId);
    List<Long> getConversationsByCustomer(Long customerId);
    Page<ChatResponse> getMessagesByConversationPaged(Long conversationId, int page, int size);
    List<ConversationResponse> getConversationsForEmp(List<ConversationStatus> statuses);
    List<ConversationResponse> getConversationsForEmp(List<ConversationStatus> statuses, LocalDateTime fromDate, LocalDateTime toDate);
    public void markCustomerMessagesAsRead(Long conversationId);
    void claimConversationAsEmp(Long conversationId, Long employeeId);
    void releaseConversationToAI(Long conversationId);
}