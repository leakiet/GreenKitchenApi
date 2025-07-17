package com.greenkitchen.portal.services;

import java.util.List;
import java.util.Map;

import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;

public interface EmployeeChatService {
    List<Long> getConversationsByEmployee(Long employeeId);
    ChatResponse sendEmployeeMessage(Long employeeId, ChatRequest request);
    Map<String, Object> getConversationInfo(Long conversationId);
}