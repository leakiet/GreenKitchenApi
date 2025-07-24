package com.greenkitchen.portal.services.impl;

import java.time.LocalDateTime;
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

import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.ConversationResponse;
import com.greenkitchen.portal.entities.ChatMessage;
import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.ConversationStatus;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.Employee;
import com.greenkitchen.portal.entities.SenderType;
import com.greenkitchen.portal.repositories.ChatMessageRepository;
import com.greenkitchen.portal.repositories.ConversationRepository;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.EmployeeRepository;
import com.greenkitchen.portal.services.ChatService;

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
	private final SimpMessagingTemplate messagingTemplate;

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
					LocalDateTime.now());
		}

		if ("/backtoAI".equals(content)) {
			conv.setStatus(ConversationStatus.AI);
			conversationRepo.saveAndFlush(conv);
			log.info("Đã cập nhật trạng thái AI, sẽ gửi notify!");
			messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
			return new ChatResponse(null, conv.getId(), "SYSTEM", "SYSTEM", "Chuyển về AI thành công.",
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

		String aiContent = callAi(content, request.getLang());
		ChatMessage aiMsg = buildMessage(null, null, conv, "AI", SenderType.AI, true, aiContent);
		chatMessageRepo.save(aiMsg);

		ChatResponse resp = mapper.map(aiMsg, ChatResponse.class);
		resp.setSenderRole(SenderType.AI.name());
		messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
		return resp;
	}

	private ChatResponse handleEmployeeMessage(Long actorId, ChatRequest request, Conversation conv) {
		Employee emp = employeeRepo.findById(actorId)
				.orElseThrow(() -> new EntityNotFoundException("Employee không tồn tại"));
		if (conv.getStatus() == ConversationStatus.AI) {
			throw new IllegalStateException("Không thể gửi tin nhắn khi conversation đang ở chế độ AI.");
		}
		if (conv.getStatus() != ConversationStatus.EMP) {
			conv.setStatus(ConversationStatus.EMP);
			conv.setEmployee(emp);
			conversationRepo.saveAndFlush(conv); // <--- Đảm bảo commit trước
			messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId()); // <--- Thông báo cho sidebar EMP
																					// realtime
		}

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
		Page<ChatMessage> msgPage = chatMessageRepo.findByConversationIdOrderByTimestampDesc(conversationId,
				PageRequest.of(page, size));
		return msgPage.map(m -> {
			ChatResponse resp = mapper.map(m, ChatResponse.class);
			// Dynamic sender name
			if (m.getSenderType() == SenderType.CUSTOMER && m.getCustomer() != null) {
				resp.setSenderName(m.getCustomer().getFirstName());
			}
			if (m.getSenderType() == SenderType.EMP && m.getEmployee() != null) {
				resp.setSenderName(m.getEmployee().getFirstName());
			}

			return resp;
		});
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
		Conversation conv = conversationRepo.findById(conversationId)
				.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
		chatMessageRepo.markMessagesAsRead(conv, SenderType.CUSTOMER);
	}

	private String callAi(String prompt, String lang) {
		return chatClient.prompt().system(
				"Bạn là chuyên gia tư vấn về ăn uống lành mạnh và thực phẩm sạch của Green Kitchen. Luôn ưu tiên trả lời theo hướng dinh dưỡng, sức khỏe, hạn chế dầu mỡ, ưu tiên món ăn tốt cho người ăn kiêng, người già, trẻ em.")
				.user(prompt).call().content();

	}
}