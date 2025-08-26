package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.ConversationResponse;
import com.greenkitchen.portal.dtos.MenuMealLiteResponse;
import com.greenkitchen.portal.entities.ChatMessage;
import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.enums.ConversationStatus;
import com.greenkitchen.portal.enums.SenderType;
import com.greenkitchen.portal.repositories.ChatMessageRepository;
import com.greenkitchen.portal.repositories.ConversationRepository;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.services.ChatQueryService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatQueryServiceImpl implements ChatQueryService {

    private final ChatMessageRepository chatMessageRepo;
    private final ConversationRepository conversationRepo;
    private final CustomerRepository customerRepo;
    private final ModelMapper mapper;

    @Override
    public List<ChatResponse> getMessagesByConversation(Long conversationId) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
        return chatMessageRepo.findByConversation(conv).stream().map(m -> {
            ChatResponse resp = mapper.map(m, ChatResponse.class);
            resp.setSenderRole(m.getSenderType() != null ? m.getSenderType().name() : null);

            if (m.getSenderType() == SenderType.CUSTOMER && m.getCustomer() != null) {
                resp.setSenderName(m.getCustomer().getFirstName());
            }
            if (m.getSenderType() == SenderType.EMP && m.getEmployee() != null) {
                resp.setSenderName(m.getEmployee().getFirstName());
            }

            if (m.getMenuJson() != null) {
                try {
                    ObjectMapper om = new ObjectMapper();
                    List<MenuMealLiteResponse> menuList = om.readerForListOf(MenuMealLiteResponse.class)
                            .readValue(m.getMenuJson());
                    resp.setMenu(menuList);
                } catch (Exception ex) {
                    log.warn("Parse menuJson fail: {}", ex.getMessage());
                    resp.setMenu(null);
                }
            }
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<ChatResponse> getMessagesByConversationPaged(Long conversationId, int page, int size) {
        Page<ChatMessage> msgPage = chatMessageRepo.findByConversationIdOrderByTimestampDesc(conversationId,
                PageRequest.of(page, size));

        return msgPage.map(m -> {
            ChatResponse resp = mapper.map(m, ChatResponse.class);
            resp.setSenderRole(m.getSenderType() != null ? m.getSenderType().name() : null);

            if (m.getSenderType() == SenderType.CUSTOMER && m.getCustomer() != null) {
                resp.setSenderName(m.getCustomer().getFirstName());
            } else if (m.getSenderType() == SenderType.EMP && m.getEmployee() != null) {
                resp.setSenderName(m.getEmployee().getFirstName());
            }

            if (m.getMenuJson() != null) {
                try {
                    ObjectMapper om = new ObjectMapper();
                    List<MenuMealLiteResponse> menuList = om.readerForListOf(MenuMealLiteResponse.class)
                            .readValue(m.getMenuJson());
                    resp.setMenu(menuList);
                } catch (Exception ex) {
                    log.warn("Parse menuJson fail: {}", ex.getMessage());
                    resp.setMenu(null);
                }
            }
            return resp;
        });
    }

    @Override
    public List<Long> getConversationsByCustomer(Long customerId) {
        Customer c = customerRepo.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer không tồn tại"));
        return conversationRepo.findByCustomer(c).stream().map(Conversation::getId).toList();
    }

    @Override
    public List<ConversationResponse> getConversationsForEmp(List<ConversationStatus> statuses) {
        List<Conversation> convs = conversationRepo.findByStatusInOrderByUpdatedAtDesc(statuses);

        return convs.stream().map(conv -> {
            String customerName = (conv.getCustomer() != null) ? conv.getCustomer().getFirstName() : "Khách vãng lai";
            String lastMsg = "";
            String lastMsgTime = "";
            if (conv.getMessages() != null && !conv.getMessages().isEmpty()) {
                ChatMessage latest = conv.getMessages().get(conv.getMessages().size() - 1);
                lastMsg = latest.getContent();
                lastMsgTime = latest.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM"));
            }
            int unreadCount = chatMessageRepo.countByConversationAndSenderTypeAndIsReadFalse(conv, SenderType.CUSTOMER);
            return new ConversationResponse(conv.getId(), customerName, conv.getStatus().name(), lastMsg, lastMsgTime,
                    unreadCount);
        }).toList();
    }
}
