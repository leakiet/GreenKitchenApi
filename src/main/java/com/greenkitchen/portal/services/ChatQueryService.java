package com.greenkitchen.portal.services;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.ConversationResponse;
import com.greenkitchen.portal.enums.ConversationStatus;

public interface ChatQueryService {
    List<ChatResponse> getMessagesByConversation(Long conversationId);
    Page<ChatResponse> getMessagesByConversationPaged(Long conversationId, int page, int size);
    List<Long> getConversationsByCustomer(Long customerId);
    List<ConversationResponse> getConversationsForEmp(List<ConversationStatus> statuses);
    List<ConversationResponse> getConversationsForEmp(List<ConversationStatus> statuses, LocalDateTime fromDate, LocalDateTime toDate);
}
