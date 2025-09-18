package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.greenkitchen.portal.dtos.MenuMealsAiResponse;
import com.greenkitchen.portal.entities.ChatMessage;
import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.repositories.ChatMessageRepository;
import com.greenkitchen.portal.repositories.ConversationRepository;
import com.greenkitchen.portal.services.ChatSummaryService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatSummaryServiceImpl implements ChatSummaryService {

    private static final Logger log = LoggerFactory.getLogger(ChatSummaryServiceImpl.class);

    private final ConversationRepository conversationRepo;
    private final ChatMessageRepository chatMessageRepo;
    private final ChatClient chatClient;
    private final PlatformTransactionManager transactionManager;

    @Override
    public String buildContextForAi(Long conversationId, String currentUserMessage) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));

        List<ChatMessage> recent = chatMessageRepo.findTop10ByConversationOrderByTimestampDesc(conv);
        java.util.Collections.reverse(recent);

        StringBuilder sb = new StringBuilder();
        if (conv.getLongTermSummary() != null && !conv.getLongTermSummary().isBlank()) {
            sb.append("<<<SUMMARY>>>\n").append(conv.getLongTermSummary()).append("\n<<<END_SUMMARY>>>\n\n");
        }

        sb.append("<<<RECENT_MESSAGES>>>\n");
        for (ChatMessage msg : recent) {
            String role = switch (msg.getSenderType().name()) {
                case "CUSTOMER" -> "user";
                case "AI" -> "assistant";
                case "EMP" -> "employee";
                default -> "other";
            };
            sb.append(role).append("|").append(msg.getSenderName()).append("| ")
              .append(msg.getContent() == null ? "" : msg.getContent().replace("\n", " ").trim())
              .append("\n");
        }
        sb.append("<<<END_RECENT_MESSAGES>>>\n\n");

        if (currentUserMessage != null && !currentUserMessage.isBlank()) {
            sb.append("<<<CURRENT_USER_MESSAGE>>>\n").append(currentUserMessage.trim()).append("\n<<<END_CURRENT_USER_MESSAGE>>>\n");
        }

        return sb.toString();
    }

    @Override
    public void summarizeIncrementally(Long conversationId) {
        TransactionTemplate txRead = new TransactionTemplate(transactionManager);
        txRead.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Conversation conv = txRead.execute(status -> conversationRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại")));

        Long lastId = conv.getLastSummarizedMessageId();

        List<ChatMessage> all = chatMessageRepo.findByConversation(conv);
        if (all == null || all.isEmpty()) return;

        List<ChatMessage> unsummarized = all.stream()
                .filter(m -> lastId == null || m.getId() > lastId)
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .collect(Collectors.toList());

        if (unsummarized.isEmpty()) return;

        String previous = conv.getLongTermSummary() == null ? "" : conv.getLongTermSummary();
        String prompt = buildSummarizePrompt(previous, unsummarized);

        String newSummary;
        try {
            MenuMealsAiResponse aiResp = chatClient.prompt()
                    .system("Bạn là trợ lý tạo tóm tắt ngắn gọn, chính xác, giữ ý định và các ràng buộc dinh dưỡng quan trọng của cuộc hội thoại.")
                    .user(prompt)
                    .call()
                    .entity(new ParameterizedTypeReference<MenuMealsAiResponse>() {});
            newSummary = aiResp.getContent();
        } catch (Exception e) {
            log.warn("Summarize failed: {}", e.getMessage());
            return;
        }

        Long newestId = unsummarized.get(unsummarized.size() - 1).getId();

        TransactionTemplate txWrite = new TransactionTemplate(transactionManager);
        txWrite.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txWrite.execute(status -> {
            Conversation c = conversationRepo.findById(conversationId)
                    .orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
            c.setLongTermSummary(newSummary);
            c.setLastSummarizedMessageId(newestId);
            conversationRepo.saveAndFlush(c);
            return null;
        });
    }

    private String buildSummarizePrompt(String previousSummary, List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("<<<PREVIOUS_SUMMARY>>>\n").append(previousSummary == null ? "" : previousSummary).append("\n<<<END_PREVIOUS_SUMMARY>>>\n\n");
        sb.append("Hãy cập nhật tóm tắt ngắn gọn, ghi nhớ: mục tiêu sức khỏe, sở thích, dị ứng, món đã tư vấn, quyết định đặt món nếu có.\n\n");
        sb.append("<<<NEW_MESSAGES>>>\n");
        for (ChatMessage m : messages) {
            String role = switch (m.getSenderType()) {
                case CUSTOMER -> "user";
                case AI -> "assistant";
                case EMP -> "employee";
                default -> "other";
            };
            sb.append(role).append(": ")
              .append(m.getContent() == null ? "" : m.getContent().replace("\n", " ").trim())
              .append("\n");
        }
        sb.append("<<<END_NEW_MESSAGES>>>\n\n");
        sb.append("Xuất duy nhất phần tóm tắt mới (không thêm meta).\n");
        return sb.toString();
    }
}


