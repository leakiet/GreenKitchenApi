package com.greenkitchen.portal.services.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.io.IOException;

import org.apache.commons.lang3.EnumUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.entities.ChatMessage;
import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.Employee;
import com.greenkitchen.portal.enums.ConversationStatus;
import com.greenkitchen.portal.enums.SenderType;
import com.greenkitchen.portal.repositories.ChatMessageRepository;
import com.greenkitchen.portal.repositories.ConversationRepository;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.EmployeeRepository;
import com.greenkitchen.portal.services.ChatCommandService;
import com.greenkitchen.portal.tools.MenuTools;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatCommandServiceImpl implements ChatCommandService {

    private final ChatClient chatClient;
    private static final Logger log = LoggerFactory.getLogger(ChatCommandServiceImpl.class);
    private final ChatMessageRepository chatMessageRepo;
    private final ConversationRepository conversationRepo;
    private final CustomerRepository customerRepo;
    private final EmployeeRepository employeeRepo;
    private final ModelMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final MenuTools menuTools;

    ObjectMapper om = new ObjectMapper();

    @Override
    public ChatResponse sendMessage(Long actorId, ChatRequest request) {
        validateRequest(request);
        SenderType senderType = SenderType.valueOf(request.getSenderRole().toUpperCase());
        Conversation conv = createOrGetConversation(actorId, senderType, request.getConversationId());

        return switch (senderType) {
            case CUSTOMER -> handleCustomerMessage(actorId, request, conv);
            case EMP -> handleEmployeeMessage(actorId, request, conv);
            default -> throw new IllegalArgumentException("SenderRole không hợp lệ: " + senderType);
        };
    }

    private ChatResponse handleCustomerMessage(Long actorId, ChatRequest request, Conversation conv) {
        String content = request.getContent();

        if ("/meet_emp".equals(content)) {
            conv.setStatus(ConversationStatus.WAITING_EMP);
            conversationRepo.saveAndFlush(conv);
            messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
            return new ChatResponse(null, conv.getId(), SenderType.SYSTEM.name(), "SYSTEM",
                    "Yêu cầu đã gửi, vui lòng chờ nhân viên.", null, LocalDateTime.now());
        }

        if ("/backtoAI".equals(content)) {
            conv.setStatus(ConversationStatus.AI);
            conv.setEmployee(null);
            conversationRepo.saveAndFlush(conv);
            messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
            return new ChatResponse(null, conv.getId(), SenderType.SYSTEM.name(), "SYSTEM",
                    "Chuyển về AI thành công.", null, LocalDateTime.now());
        }

        if (conv.getStatus() != ConversationStatus.AI) {
            Customer customer = customerRepo.findById(actorId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer không tồn tại"));
            ChatMessage msg = buildMessage(customer, null, conv, customer.getFirstName(), SenderType.CUSTOMER, false,
                    content);
            chatMessageRepo.save(msg);
            ChatResponse resp = mapper.map(msg, ChatResponse.class);
            resp.setSenderRole(SenderType.CUSTOMER.name());
            messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
            messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
            return resp;
        }

        Customer customer = (actorId != null) ? customerRepo.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Customer không tồn tại")) : null;

        ChatMessage userMsg = buildMessage(customer, null, conv, customer != null ? customer.getFirstName() : "Guest",
                SenderType.CUSTOMER, false, content);
        chatMessageRepo.save(userMsg);

        ChatResponse userResp = mapper.map(userMsg, ChatResponse.class);
        userResp.setSenderRole(SenderType.CUSTOMER.name());
        messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), userResp);
        List<ChatMessage> last20Msgs = chatMessageRepo.findTop20ByConversationOrderByTimestampDesc(conv);
        Collections.reverse(last20Msgs);

        StringBuilder sb = new StringBuilder();
        sb.append("<<<HISTORY>>>\n"); // start sentinel
        for (ChatMessage msg : last20Msgs) {
            String role = switch (msg.getSenderType().name()) {
                case "CUSTOMER" -> "user";
                case "AI"       -> "assistant";
                case "EMP"      -> "employee";
                default         -> "other";
            };
            sb.append(role).append("|")
              .append(msg.getSenderName()).append("| ")
              .append(msg.getContent().replace("\n", " ").trim())
              .append("\n");
        }
        sb.append("<<<END_HISTORY>>>\n\n");

        sb.append("<<<CURRENT_USER_MESSAGE>>>\n")
          .append(request.getContent().trim())
          .append("\n<<<END_CURRENT_USER_MESSAGE>>>\n");

        String context = sb.toString();
        String aiContent = callAi(context, request.getLang());


        String respContent = aiContent;
        
        // Phân tích JSON từ AI
        List<MenuMealResponse> menuList = null;
        try {
            String trimmed = aiContent.trim();

            // Xử lý nếu AI trả về markdown kiểu ```json\n...\n```
            if (trimmed.startsWith("```")) {
                trimmed = trimmed.replaceAll("(?s)^```(?:json)?\\s*", "")
                                 .replaceAll("\\s*```$", "");
            }

            JsonNode root = om.readTree(trimmed);

            if (root.has("menu") && root.get("menu").isArray()) {
                respContent = root.path("content").asText(""); // fallback ""
                menuList = om.readerForListOf(MenuMealResponse.class).readValue(root.get("menu"));
            }
            log.debug("JSON sau khi clean:\n{}", trimmed);

        } catch (Exception e) {
            log.error("Lỗi parse AI JSON:\n{}", aiContent, e);
            
        }



        ChatMessage aiMsg = buildMessage(null, null, conv, "AI", SenderType.AI, true, respContent);
        if (menuList != null && !menuList.isEmpty()) {
            try {
                aiMsg.setMenuJson(om.writeValueAsString(menuList));
            } catch (JsonProcessingException e) {
                log.warn("Không lưu được menu JSON: {}", e.getMessage());
            }
        }
        chatMessageRepo.save(aiMsg);

        ChatResponse resp = mapper.map(aiMsg, ChatResponse.class);
        resp.setSenderRole(SenderType.AI.name());
        resp.setMenu(menuList);

        messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
        return resp;
    }

    private ChatResponse handleEmployeeMessage(Long actorId, ChatRequest request, Conversation conv) {
        Employee emp = employeeRepo.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Employee không tồn tại"));

        conv.setStatus(ConversationStatus.EMP);
        conv.setEmployee(emp);
        conversationRepo.saveAndFlush(conv);
        messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());

        ChatMessage empMsg = buildMessage(null, emp, conv, emp.getFirstName(), SenderType.EMP, false,
                request.getContent());
        chatMessageRepo.save(empMsg);

        ChatResponse resp = mapper.map(empMsg, ChatResponse.class);
        resp.setSenderRole(SenderType.EMP.name());
        messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
        return resp;
    }

    private Conversation createOrGetConversation(Long actorId, SenderType senderType, Long convId) {
        if (convId != null) {
            return conversationRepo.findById(convId)
                    .orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
        }
        Conversation conv = new Conversation();
        conv.setStartTime(LocalDateTime.now());
        conv.setStatus(ConversationStatus.AI);
        if (senderType == SenderType.CUSTOMER && actorId != null) {
            Customer customer = customerRepo.findById(actorId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer không tồn tại"));
            conv.setCustomer(customer);
        }
        return conversationRepo.save(conv);
    }

    private ChatMessage buildMessage(Customer customer, Employee employee, Conversation conv, String senderName,
            SenderType senderType, boolean isFromAI, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setCustomer(customer);
        msg.setEmployee(employee);
        msg.setConversation(conv);
        msg.setSenderName(senderName);
        msg.setSenderType(senderType);
        msg.setIsFromAI(isFromAI);
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now());
        msg.setIsRead(senderType != SenderType.CUSTOMER);
        return msg;
    }

    private void validateRequest(ChatRequest request) {
        if (request.getSenderRole() == null || request.getSenderRole().isBlank()) {
            throw new IllegalArgumentException("SenderRole không được để trống");
        }
        if (!EnumUtils.isValidEnumIgnoreCase(SenderType.class, request.getSenderRole())) {
            throw new IllegalArgumentException("SenderRole không hợp lệ: " + request.getSenderRole());
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống");
        }
    }

    // Gọi AI với prompt và ngôn ngữ (tái sử dụng menuTools)
    private String callAi(String prompt, String lang) {
        String systemPrompt;
        try {
            systemPrompt = loadPrompt("PromtAIGreenKitchen.md");
        } catch (IOException e) {
            log.error("Không thể tải prompt từ file: " + e.getMessage());
            systemPrompt = "Bạn là nhân viên tư vấn dinh dưỡng & CSKH của thương hiệu thực phẩm sạch Green Kitchen...";
        }
        return chatClient.prompt().system(systemPrompt).tools(menuTools).user(prompt).call().content();
    }

    private String loadPrompt(String fileName) throws IOException {
        org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource(
                "prompts/" + fileName);
        try (java.io.InputStream is = resource.getInputStream()) {
            return org.springframework.util.StreamUtils.copyToString(is, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @Override
    public void markCustomerMessagesAsRead(Long conversationId) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
        chatMessageRepo.markMessagesAsRead(conv, SenderType.CUSTOMER);
    }

    @Override
    public void claimConversationAsEmp(Long conversationId, Long employeeId) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
        Employee emp = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee không tồn tại"));
        conv.setStatus(ConversationStatus.EMP);
        conv.setEmployee(emp);
        conversationRepo.saveAndFlush(conv);
        messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
    }

    @Override
    public void releaseConversationToAI(Long conversationId) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
        conv.setStatus(ConversationStatus.AI);
        conv.setEmployee(null);
        conversationRepo.saveAndFlush(conv);
        messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
    }
}
