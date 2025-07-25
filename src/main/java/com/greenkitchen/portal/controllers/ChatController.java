package com.greenkitchen.portal.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.ConversationResponse;
import com.greenkitchen.portal.dtos.ConversationResquest;
import com.greenkitchen.portal.dtos.ChatPagingResponse;
import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.ConversationStatus;
import com.greenkitchen.portal.entities.SenderType;
import com.greenkitchen.portal.repositories.ConversationRepository;
import com.greenkitchen.portal.services.ChatService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "${app.frontend.url}")
@RequestMapping("/apis/v1/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final ConversationRepository conversationRepo;
	Logger logger = org.slf4j.LoggerFactory.getLogger(ChatController.class);

	@PostMapping("/send")
	public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request,
			@RequestParam(value = "customerId", required = false) Long customerId,
			@RequestParam(value = "employeeId", required = false) Long employeeId) {

		// 1. Validate senderRole với enum
		SenderType role;
		try {
			role = SenderType.valueOf(request.getSenderRole().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "senderRole không hợp lệ");
		}

		// 2. Xác định actorId, phân quyền và lỗi rõ ràng
		Long actorId;
		switch (role) {
		case EMP:
			actorId = employeeId;
			if (actorId == null) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Employee chưa đăng nhập");
			}
			break;
		case CUSTOMER:
			actorId = customerId; // có thể null = guest
			if ("/meet_emp".equalsIgnoreCase(request.getContent()) && actorId == null) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"Guest không được yêu cầu gặp nhân viên. Vui lòng đăng nhập.");
			}
			break;
		default:
			// Chặn AI hoặc SYSTEM do client không cần gửi
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không được phép gửi tin với senderRole: " + role);
		}

		// 3. Log đủ context để debug
		logger.info("SEND_MESSAGE role={} actorId={} convId={}", role, actorId, request.getConversationId());

		// 4. Gửi tới service
		ChatResponse resp = chatService.sendMessage(actorId, request);
		return ResponseEntity.ok(resp);
	}

	@GetMapping("/messages-paged")
	public ResponseEntity<ChatPagingResponse<ChatResponse>> getMessagesPaged(
			@RequestParam("conversationId") Long conversationId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size) {
		Page<ChatResponse> pg = chatService.getMessagesByConversationPaged(conversationId, page, size);
		ChatPagingResponse<ChatResponse> resp = new ChatPagingResponse<>(pg.getContent(), pg.getNumber(), pg.getSize(),
				pg.getTotalElements(), pg.getTotalPages(), pg.isLast());
		return ResponseEntity.ok(resp);
	}

	@GetMapping("/messages")
	public ResponseEntity<List<ChatResponse>> getMessages(@RequestParam("conversationId") Long conversationId) {
		return ResponseEntity.ok(chatService.getMessagesByConversation(conversationId));
	}

	@GetMapping("/conversations")
	public ResponseEntity<List<Long>> getConversations(@RequestParam("customerId") Long customerId) {
		return ResponseEntity.ok(chatService.getConversationsByCustomer(customerId));
	}

	@GetMapping("/employee/conversations")
	public ResponseEntity<List<ConversationResponse>> getConversationsForEmp(
			@RequestParam(value = "status", required = false) List<ConversationStatus> statuses) {
		if (statuses == null || statuses.isEmpty()) {
			statuses = List.of(ConversationStatus.EMP, ConversationStatus.WAITING_EMP);
		}
		// Gọi sang service mới đã mapping unreadCount
		List<ConversationResponse> resp = chatService.getConversationsForEmp(statuses);
		return ResponseEntity.ok(resp);
	}

	@GetMapping("/status")
	public ResponseEntity<String> getConversationStatus(@RequestParam("conversationId") Long conversationId) {
		Conversation conv = conversationRepo.findById(conversationId)
				.orElseThrow(() -> new EntityNotFoundException("Không tồn tại"));
		return ResponseEntity.ok(conv.getStatus().name()); // Trả về "AI", "EMP", "WAITING_EMP"
	}
	@PostMapping("/mark-read")
	public ResponseEntity<Void> markRead(@RequestParam("conversationId") Long conversationId) {
	    chatService.markCustomerMessagesAsRead(conversationId);
	    return ResponseEntity.ok().build();
	}

}
