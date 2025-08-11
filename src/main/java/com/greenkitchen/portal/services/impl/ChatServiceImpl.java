package com.greenkitchen.portal.services.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.ConversationResponse;
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
import com.greenkitchen.portal.services.ChatService;
import com.greenkitchen.portal.tools.MenuTools;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

	private final ChatClient chatClient;
	private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);
	private final ChatMessageRepository chatMessageRepo;
	private final ConversationRepository conversationRepo;
	private final CustomerRepository customerRepo;
	private final EmployeeRepository employeeRepo;
	private final ModelMapper mapper;
	ObjectMapper om = new ObjectMapper();
	private final SimpMessagingTemplate messagingTemplate;
	// Thêm MenuTools để sử dụng trong các lệnh đặc biệt cho AI
	private final MenuTools menuTools;

	@Override
	public ChatResponse sendMessage(Long actorId, ChatRequest request) {
		validateRequest(request);
		SenderType senderType = SenderType.valueOf(request.getSenderRole().toUpperCase());
		Conversation conv = createOrGetConversation(actorId, senderType, request.getConversationId());

		switch (senderType) {
		case CUSTOMER:
			return handleCustomerMessage(actorId, request, conv);
		case EMP:
			return handleEmployeeMessage(actorId, request, conv);
		default:
			throw new IllegalArgumentException("SenderRole không hợp lệ: " + senderType);
		}
	}

	private ChatResponse handleCustomerMessage(Long actorId, ChatRequest request, Conversation conv) {
		String content = request.getContent();

		// 1. Xử lý lệnh đặc biệt trước
		if ("/meet_emp".equals(content)) {
			conv.setStatus(ConversationStatus.WAITING_EMP);
			conversationRepo.saveAndFlush(conv);
			log.info("Đã cập nhật trạng thái WAITING_EMP, sẽ gửi notify!");
			messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
			return new ChatResponse(null, conv.getId(), "SYSTEM", "SYSTEM", "Yêu cầu đã gửi, vui lòng chờ nhân viên.",
					null, LocalDateTime.now());
		}

		if ("/backtoAI".equals(content)) {
			conv.setStatus(ConversationStatus.AI);
			conversationRepo.saveAndFlush(conv);
			log.info("Đã cập nhật trạng thái AI, sẽ gửi notify!");
			messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
			return new ChatResponse(null, conv.getId(), "SYSTEM", "SYSTEM", "Chuyển về AI thành công.", null,
					LocalDateTime.now());
		}

		// 2. Còn lại xử lý tin nhắn bình thường như code của bạn
		if (conv.getStatus() != ConversationStatus.AI) {
			Customer customer = customerRepo.findById(actorId)
					.orElseThrow(() -> new EntityNotFoundException("Customer không tồn tại"));
			ChatMessage msg = buildMessage(customer, null, conv, customer.getFirstName(), SenderType.CUSTOMER, false,
					content);
			chatMessageRepo.save(msg);
			ChatResponse resp = mapper.map(msg, ChatResponse.class);
			resp.setSenderRole(SenderType.CUSTOMER.name());
			messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
			// Thêm dòng này: Gửi notify để sidebar (badge) cập nhật
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

		List<ChatMessage> last10Msgs = chatMessageRepo.findTop20ByConversationOrderByTimestampDesc(conv);
		Collections.reverse(last10Msgs); // đảo thứ tự cho đúng flow

		// Xây prompt context cho AI (tuỳ bạn định dạng, ví dụ)
		StringBuilder context = new StringBuilder();
		context.append("Conversation history:\n");
		for (ChatMessage msg : last10Msgs) {
			context.append(msg.getSenderName()).append(" (").append(msg.getSenderType().name()).append("): ")
					.append(msg.getContent()).append("\n");
		}
		context.append("\nLatest user message:\n").append(request.getContent());

		String aiContent = callAi(context.toString(), request.getLang());

		String respContent = aiContent;
		List<MenuMealResponse> menuList = null;

		try {
			JsonNode root = om.readTree(aiContent);
			if (root.has("menu") && root.get("menu").isArray()) {
				respContent = root.has("content") ? root.get("content").asText() : "";
				menuList = om.readerForListOf(MenuMealResponse.class).readValue(root.get("menu"));
			}
		} catch (Exception e) {
			// Đoạn này KHÔNG nên báo lỗi nghiêm trọng nếu aiContent không phải JSON!
			log.info("AI response không phải JSON, sẽ dùng làm text thường: {}", aiContent);
			respContent = aiContent;
			menuList = null;
		}

		ChatMessage aiMsg = buildMessage(null, null, conv, "AI", SenderType.AI, true, respContent);

		if (menuList != null && !menuList.isEmpty()) {
			// Lưu menu JSON vào field mới (phải khai báo @Lob private String menuJson; ở
			// entity ChatMessage)
			try {
				aiMsg.setMenuJson(om.writeValueAsString(menuList));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		chatMessageRepo.save(aiMsg);

		ChatResponse resp = mapper.map(aiMsg, ChatResponse.class);
		resp.setSenderRole(SenderType.AI.name());
		resp.setMenu(menuList); // <--- Quan trọng! FE sẽ nhận được menu ở đây.

		messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
		return resp;

	}

	private ChatResponse handleEmployeeMessage(Long actorId, ChatRequest request, Conversation conv) {
		Employee emp = employeeRepo.findById(actorId)
				.orElseThrow(() -> new EntityNotFoundException("Employee không tồn tại"));

		conv.setStatus(ConversationStatus.EMP);
		conv.setEmployee(emp);
		conversationRepo.saveAndFlush(conv); // <--- Đảm bảo commit trước
		messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId()); // <--- Thông báo cho sidebar EMP
																				// realtime

		ChatMessage empMsg = buildMessage(null, emp, conv, emp.getFirstName(), SenderType.EMP, false,
				request.getContent());
		chatMessageRepo.save(empMsg);

		ChatResponse resp = mapper.map(empMsg, ChatResponse.class);
		resp.setSenderRole(SenderType.EMP.name());
		messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
		return resp;
	}

	@Override
	public List<ChatResponse> getMessagesByConversation(Long conversationId) {
		Conversation conv = conversationRepo.findById(conversationId)
				.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
		return chatMessageRepo.findByConversation(conv).stream().map(m -> {
			ChatResponse resp = mapper.map(m, ChatResponse.class);
			resp.setSenderRole(m.getSenderType() != null ? m.getSenderType().name() : null);

			// Lấy tên mới nhất từ bảng Customer/Employee nếu có
			if (m.getSenderType() == SenderType.CUSTOMER && m.getCustomer() != null) {
				resp.setSenderName(m.getCustomer().getFirstName());
			}
			if (m.getSenderType() == SenderType.EMP && m.getEmployee() != null) {
				resp.setSenderName(m.getEmployee().getFirstName());
			}

			// ==== BỔ SUNG ĐOẠN NÀY ====
			if (m.getMenuJson() != null) {
				try {
					ObjectMapper om = new ObjectMapper();
					List<MenuMealResponse> menuList = om.readerForListOf(MenuMealResponse.class)
							.readValue(m.getMenuJson());
					resp.setMenu(menuList);
				} catch (Exception ex) {
					log.warn("Parse menuJson fail: {}", ex.getMessage());
					resp.setMenu(null);
				}
			}
			// ==== HẾT BỔ SUNG ====

			// AI/SYSTEM giữ nguyên
			return resp;
		}).collect(Collectors.toList());
	}

	@Override
	public List<Long> getConversationsByCustomer(Long customerId) {
		Customer c = customerRepo.findById(customerId)
				.orElseThrow(() -> new EntityNotFoundException("Customer không tồn tại"));
		return conversationRepo.findByCustomer(c).stream().map(Conversation::getId).toList();
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

		msg.setIsRead(senderType != SenderType.CUSTOMER); // <-- CUSTOMER gửi thì isRead=false, còn lại true
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

	@Override
	public Page<ChatResponse> getMessagesByConversationPaged(Long conversationId, int page, int size) {
		log.info("getMessagesByConversationPaged called with conversationId={}, page={}, size={}", conversationId, page,
				size);

		Page<ChatMessage> msgPage = chatMessageRepo.findByConversationIdOrderByTimestampDesc(conversationId,
				PageRequest.of(page, size));
		log.info("Found {} messages in conversationId={}", msgPage.getTotalElements(), conversationId);

		return msgPage.map(m -> {
			log.debug("Mapping messageId={}, senderType={}, customer={}, employee={}", m.getId(), m.getSenderType(),
					m.getCustomer() != null ? m.getCustomer().getFirstName() : null,
					m.getEmployee() != null ? m.getEmployee().getFirstName() : null);

			ChatResponse resp = mapper.map(m, ChatResponse.class);

			resp.setSenderRole(m.getSenderType() != null ? m.getSenderType().name() : null);

			if (m.getSenderType() == SenderType.CUSTOMER && m.getCustomer() != null) {
				resp.setSenderName(m.getCustomer().getFirstName());
			} else if (m.getSenderType() == SenderType.EMP && m.getEmployee() != null) {
				resp.setSenderName(m.getEmployee().getFirstName());
			}

			// ===== BỔ SUNG PHẦN NÀY =====
			if (m.getMenuJson() != null) {
				try {
					ObjectMapper om = new ObjectMapper();
					List<MenuMealResponse> menuList = om.readerForListOf(MenuMealResponse.class)
							.readValue(m.getMenuJson());
					resp.setMenu(menuList);
				} catch (Exception ex) {
					log.warn("Parse menuJson fail: {}", ex.getMessage());
					resp.setMenu(null);
				}
			}
			// ===== HẾT BỔ SUNG =====

			log.debug("ChatResponse mapped: id={}, senderRole={}, senderName={}", resp.getId(), resp.getSenderRole(),
					resp.getSenderName());

			return resp;
		});
	}

	@Override
	public List<ConversationResponse> getConversationsForEmp(List<ConversationStatus> statuses) {
		if (statuses == null || statuses.isEmpty()) {
			throw new IllegalArgumentException("Danh sách trạng thái không được để trống");
		}
		List<Conversation> convs = conversationRepo.findByStatusInOrderByUpdatedAtDesc(statuses);

		return convs.stream().map(conv -> {
			String customerName = (conv.getCustomer() != null) ? conv.getCustomer().getFirstName() : "Khách vãng lai";
			String lastMsg = "";
			String lastMsgTime = "";
			if (conv.getMessages() != null && !conv.getMessages().isEmpty()) {
				ChatMessage latest = conv.getMessages().get(conv.getMessages().size() - 1);
				lastMsg = latest.getContent();
				// Format về String, ví dụ "12:30 17/07"
				lastMsgTime = latest.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM"));
			}
			int unreadCount = chatMessageRepo.countByConversationAndSenderTypeAndIsReadFalse(conv, SenderType.CUSTOMER);
			return new ConversationResponse(conv.getId(), customerName, conv.getStatus().name(), lastMsg, lastMsgTime,
					unreadCount);
		}).toList();
	}

	@Transactional
	@Override
	public void markCustomerMessagesAsRead(Long conversationId) {
		log.info("[mark-read] conversationId: {}", conversationId);

		if (conversationId == null) {
			log.error("conversationId null!");
			throw new IllegalArgumentException("conversationId không được null");
		}
		Conversation conv = conversationRepo.findById(conversationId).orElseThrow(() -> {
			log.error("Conversation {} không tồn tại", conversationId);
			return new EntityNotFoundException("Conversation không tồn tại");
		});

		// LOG ĐỂ XEM CONVERSATION VÀ SENDER TYPE ĐƯỢC TRUYỀN VÀO
		log.info("Marking as read for conversation: {}, senderType: CUSTOMER", conv.getId());

		chatMessageRepo.markMessagesAsRead(conv, SenderType.CUSTOMER);
	}
	
	// Tải prompt từ file trong resources
	public String loadPrompt(String fileName) throws IOException {
	    ClassPathResource resource = new ClassPathResource("prompts/" + fileName);
	    try (InputStream is = resource.getInputStream()) {
	        return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
	    }
	}
	// Gọi AI với prompt và ngôn ngữ
	private String callAi(String prompt, String lang) {
	    String systemPrompt;
	    try {
	        systemPrompt = loadPrompt("PromtAIGreenKitchen.md");
	    } catch (IOException e) {
	        // Xử lý lỗi nếu không đọc được file
	    	log.error("Không thể tải prompt từ file: " + e.getMessage());
	        systemPrompt = "Bạn là nhân viên tư vấn dinh dưỡng & CSKH của thương hiệu thực phẩm sạch Green Kitchen...";
	    }
	    return chatClient.prompt()
	            .system(systemPrompt)
	            .tools(menuTools)
	            .user(prompt)
	            .call()
	            .content();
	}


	@Override
	@Transactional
	public void claimConversationAsEmp(Long conversationId, Long employeeId) {
		log.info("claimConversationAsEmp called with conversationId={}, employeeId={}", conversationId, employeeId);

		if (conversationId == null || employeeId == null) {
			log.error("conversationId hoặc employeeId bị null");
			throw new IllegalArgumentException("conversationId và employeeId không được null");
		}

		Conversation conv = conversationRepo.findById(conversationId).orElseThrow(() -> {
			log.error("Conversation không tồn tại với id={}", conversationId);
			return new EntityNotFoundException("Conversation không tồn tại");
		});

		Employee emp = employeeRepo.findById(employeeId).orElseThrow(() -> {
			log.error("Employee không tồn tại với id={}", employeeId);
			return new EntityNotFoundException("Employee không tồn tại");
		});

		log.info("Assigning EMP={} to conversationId={}", employeeId, conversationId);
		conv.setStatus(ConversationStatus.EMP);
		conv.setEmployee(emp);
		conversationRepo.saveAndFlush(conv);

		log.info("Sending notify to /topic/emp-notify, convId={}", conv.getId());
		messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
	}

	@Override
	@Transactional
	public void releaseConversationToAI(Long conversationId) {
		if (conversationId == null) {
			throw new IllegalArgumentException("conversationId không được null");
		}
		Conversation conv = conversationRepo.findById(conversationId)
				.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
		conv.setStatus(ConversationStatus.AI);
		conv.setEmployee(null);
		conversationRepo.saveAndFlush(conv);

		// Bắn notify FE
		messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
	}

}