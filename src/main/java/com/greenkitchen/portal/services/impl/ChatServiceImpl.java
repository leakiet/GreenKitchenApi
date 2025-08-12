package com.greenkitchen.portal.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.ConversationResponse;
import com.greenkitchen.portal.enums.ConversationStatus;
import com.greenkitchen.portal.services.ChatCommandService;
import com.greenkitchen.portal.services.ChatQueryService;
import com.greenkitchen.portal.services.ChatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatCommandService commandService;
    private final ChatQueryService queryService;

    @Override
    public ChatResponse sendMessage(Long actorId, ChatRequest request) {
        return commandService.sendMessage(actorId, request);
    }

    @Override
    public List<ChatResponse> getMessagesByConversation(Long conversationId) {
        return queryService.getMessagesByConversation(conversationId);
    }

    @Override
    public List<Long> getConversationsByCustomer(Long customerId) {
        return queryService.getConversationsByCustomer(customerId);
    }

    @Override
    public Page<ChatResponse> getMessagesByConversationPaged(Long conversationId, int page, int size) {
        return queryService.getMessagesByConversationPaged(conversationId, page, size);
    }

    @Override
    public List<ConversationResponse> getConversationsForEmp(List<ConversationStatus> statuses) {
        return queryService.getConversationsForEmp(statuses);
    }

    @Override
    public void markCustomerMessagesAsRead(Long conversationId) {
        commandService.markCustomerMessagesAsRead(conversationId);
    }

    @Override
    public void claimConversationAsEmp(Long conversationId, Long employeeId) {
        commandService.claimConversationAsEmp(conversationId, employeeId);
    }

    @Override
    public void releaseConversationToAI(Long conversationId) {
        commandService.releaseConversationToAI(conversationId);
    }
}