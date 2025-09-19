package com.greenkitchen.portal.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StreamUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.CacheEvict;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.EmpNotifyPayload;
import com.greenkitchen.portal.dtos.MenuMealLiteResponse;
import com.greenkitchen.portal.dtos.MenuMealsAiResponse;
import com.greenkitchen.portal.entities.ChatMessage;
import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerReference;
import com.greenkitchen.portal.entities.Employee;
import com.greenkitchen.portal.enums.ConversationStatus;
import com.greenkitchen.portal.enums.MessageStatus;
import com.greenkitchen.portal.enums.SenderType;
import com.greenkitchen.portal.repositories.ChatMessageRepository;
import com.greenkitchen.portal.repositories.ConversationRepository;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.EmployeeRepository;
import com.greenkitchen.portal.services.ChatCommandService;
import com.greenkitchen.portal.services.CustomerReferenceService;
import com.greenkitchen.portal.services.ChatSummaryService;
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
	private final CustomerReferenceService customerReferenceService;
	private final ModelMapper mapper;
	private final SimpMessagingTemplate messagingTemplate;
	private final MenuTools menuTools;
	private final PlatformTransactionManager transactionManager;
	private final ChatSummaryService chatSummaryService;

	ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

	@Override
	@Transactional
	@CacheEvict(value = "conversations", allEntries = true)
	public ChatResponse sendMessage(Long actorId, ChatRequest request) {
		validateRequest(request);
		SenderType senderType = SenderType.valueOf(request.getSenderRole().toUpperCase());
		
		// Tạo hoặc lấy conversation trong transaction chính
		Conversation conv = createOrGetConversation(actorId, senderType, request.getConversationId());

		return switch (senderType) {
		case CUSTOMER -> handleCustomerMessage(actorId, request, conv);
		case EMP -> handleEmployeeMessageFromEmployee(actorId, request, conv);
		default -> throw new IllegalArgumentException("SenderRole không hợp lệ: " + senderType);
		};
	}

	// Tạo hoặc lấy conversation: dùng transaction REQUIRES_NEW để đảm bảo có ID trước khi lưu message
	private Conversation createOrGetConversation(Long actorId, SenderType senderType, Long convId) {
		if (convId != null) {
			return conversationRepo.findById(convId)
					.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
		}

		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		// Conversation creation should be quick; set a short timeout
		txTemplate.setTimeout(10);

		return txTemplate.execute(status -> {
			Conversation conv = new Conversation();
			conv.setStartTime(LocalDateTime.now());
			conv.setStatus(ConversationStatus.AI);

			// Hỗ trợ cả Customer đã đăng nhập và Guest chưa đăng nhập
			if (senderType == SenderType.CUSTOMER && actorId != null) {
				Customer customer = customerRepo.findById(actorId)
						.orElseThrow(() -> new EntityNotFoundException("Customer không tồn tại"));
				conv.setCustomer(customer);
			}

			return conversationRepo.saveAndFlush(conv); // Cần ID ngay để sử dụng
		});
	}

	// Transaction riêng cho command /meet_emp
	@Transactional(timeout = 30) // Tăng từ 15 lên 30 giây
	private ChatResponse handleMeetEmpCommand(Conversation conv) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		// Tránh treo khi DB bận: giới hạn 15s cho việc lưu PENDING
		txTemplate.setTimeout(15);
		
		try {
			return txTemplate.execute(status -> {
				// Kiểm tra trạng thái trước khi update để tránh update trùng
				Conversation currentConv = conversationRepo.findById(conv.getId())
						.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
				
				if (currentConv.getStatus() != ConversationStatus.WAITING_EMP) {
					currentConv.setStatus(ConversationStatus.WAITING_EMP);
					conversationRepo.save(currentConv);
				}
				
                EmpNotifyPayload payload = new EmpNotifyPayload(conv.getId(), ConversationStatus.WAITING_EMP.name(), "CUSTOMER", LocalDateTime.now());
                messagingTemplate.convertAndSend("/topic/emp-notify", payload);
				ChatResponse response = new ChatResponse();
				response.setId(null);
				response.setConversationId(conv.getId());
				response.setSenderRole(SenderType.SYSTEM.name());
				response.setSenderName("SYSTEM");
				response.setContent("Yêu cầu đã gửi, vui lòng chờ nhân viên.");
				response.setMenu(null);
				response.setTimestamp(LocalDateTime.now());
				response.setStatus(MessageStatus.SENT);
				response.setConversationStatus(ConversationStatus.WAITING_EMP.name());
				return response;
			});
		} catch (OptimisticLockingFailureException e) {
			log.warn("Optimistic locking conflict in handleMeetEmpCommand for conversation {}", conv.getId());
			throw new IllegalStateException("Conversation đang được cập nhật bởi người dùng khác, vui lòng thử lại");
		}
	}

	// Transaction riêng cho command /backtoAI
	@Transactional(timeout = 30) // Tăng từ 15 lên 30 giây
	private ChatResponse handleBackToAICommand(Conversation conv) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		try {
			return txTemplate.execute(status -> {
				// Kiểm tra trạng thái trước khi update để tránh update trùng
				Conversation currentConv = conversationRepo.findById(conv.getId())
						.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
				
				if (currentConv.getStatus() != ConversationStatus.AI) {
					currentConv.setStatus(ConversationStatus.AI);
					currentConv.setEmployee(null);
					conversationRepo.save(currentConv);
				}
				
                EmpNotifyPayload payload = new EmpNotifyPayload(conv.getId(), ConversationStatus.AI.name(), "SYSTEM", LocalDateTime.now());
                messagingTemplate.convertAndSend("/topic/emp-notify", payload);
				ChatResponse response = new ChatResponse();
				response.setId(null);
				response.setConversationId(conv.getId());
				response.setSenderRole(SenderType.SYSTEM.name());
				response.setSenderName("SYSTEM");
				response.setContent("Chuyển về AI thành công.");
				response.setMenu(null);
				response.setTimestamp(LocalDateTime.now());
				response.setStatus(MessageStatus.SENT);
				response.setConversationStatus(ConversationStatus.AI.name());
				return response;
			});
		} catch (OptimisticLockingFailureException e) {
			log.warn("Optimistic locking conflict in handleBackToAICommand for conversation {}", conv.getId());
			throw new IllegalStateException("Conversation đang được cập nhật bởi người dùng khác, vui lòng thử lại");
		}
	}

	// Transaction riêng cho message gửi đến employee
	private ChatResponse handleEmployeeMessage(Long actorId, String content, Conversation conv) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		return txTemplate.execute(status -> {
			// Chỉ kiểm tra customer nếu actorId không null (đã đăng nhập)
			final Customer customer = (actorId != null) ? customerRepo.findById(actorId)
					.orElse(null) : null;
			
			String senderName = (customer != null) ? customer.getFirstName() : "Guest";
			ChatMessage m = buildMessage(customer, null, conv, senderName,
					SenderType.CUSTOMER, false, content);
			m.setStatus(MessageStatus.SENT);
			ChatMessage msg = chatMessageRepo.save(m); // Không dùng saveAndFlush
			
			ChatResponse resp = mapper.map(msg, ChatResponse.class);
			resp.setSenderRole(SenderType.CUSTOMER.name());
			resp.setConversationStatus(conv.getStatus().name());
			messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
            EmpNotifyPayload payload = new EmpNotifyPayload(conv.getId(), conv.getStatus().name(), "CUSTOMER", LocalDateTime.now());
            messagingTemplate.convertAndSend("/topic/emp-notify", payload);
			return resp;
		});
	}

	// Wrapper class để trả về kết quả từ saveCustomerPendingMessage
	private static class PendingMessageResult {
		private final ChatResponse userResp;
		private final ChatResponse aiPendingResp;
		private final ChatMessage userMsg;
		private final ChatMessage aiMsg;
		
		public PendingMessageResult(ChatResponse userResp, ChatResponse aiPendingResp, 
								  ChatMessage userMsg, ChatMessage aiMsg) {
			this.userResp = userResp;
			this.aiPendingResp = aiPendingResp;
			this.userMsg = userMsg;
			this.aiMsg = aiMsg;
		}
		
		public ChatResponse getUserResp() { return userResp; }
		public ChatResponse getAiPendingResp() { return aiPendingResp; }
		public ChatMessage getUserMsg() { return userMsg; }
		public ChatMessage getAiMsg() { return aiMsg; }
	}
	
	// Transaction riêng cho việc lưu message PENDING của CUSTOMER
	// Đảm bảo message PENDING luôn được lưu vào DB ngay cả khi có lỗi xảy ra phía sau
	private PendingMessageResult saveCustomerPendingMessage(Long actorId, ChatRequest request, Conversation conv) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		return txTemplate.execute(status -> {
			final Customer customer = (actorId != null) ? customerRepo.findById(actorId)
					.orElse(null) : null;

			// Lưu CUSTOMER PENDING trong transaction riêng
			ChatMessage userMsg = buildMessage(customer, null, conv,
					customer != null ? customer.getFirstName() : "Guest",
					SenderType.CUSTOMER, false, request.getContent());
			userMsg.setStatus(MessageStatus.PENDING);
			userMsg = chatMessageRepo.saveAndFlush(userMsg); // Cần ID để emit

			// Lưu AI PENDING cũng trong transaction này để đảm bảo tính nhất quán
			ChatMessage aiMsg = buildMessage(null, null, conv, "AI", SenderType.AI, true, "");
			aiMsg.setStatus(MessageStatus.PENDING);
			aiMsg = chatMessageRepo.saveAndFlush(aiMsg); // Cần ID để emit và update sau

			// Emit ngay lập tức sau khi commit transaction
			ChatResponse userResp = mapper.map(userMsg, ChatResponse.class);
			userResp.setSenderRole(SenderType.CUSTOMER.name());
			userResp.setConversationStatus(conv.getStatus().name());
			messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), userResp);

			ChatResponse aiPendingResp = mapper.map(aiMsg, ChatResponse.class);
			aiPendingResp.setSenderRole(SenderType.AI.name());
			aiPendingResp.setConversationStatus(conv.getStatus().name());
			messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), aiPendingResp);

			// Trả về wrapper object chứa cả 2 message
			return new PendingMessageResult(userResp, aiPendingResp, userMsg, aiMsg);
		});
	}

	// Xử lý AI: gọi AI ngoài transaction, chỉ mở transaction ngắn để ghi DB
	private ChatResponse processAIResponse(String context, String lang, ChatMessage userMsg, ChatMessage aiMsg, Conversation conv) {
		long startTime = System.currentTimeMillis();
		log.info("🚀 Starting AI processing for conversation: {}", conv.getId());
		
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		return txTemplate.execute(status -> {
			try {
				// 1. AI call
				long aiStartTime = System.currentTimeMillis();
				log.info("🤖 Calling AI with context length: {} characters", context.length());
				
                MenuMealsAiResponse aiResp = callAi(context, lang, conv.getId());
				
				long aiDuration = System.currentTimeMillis() - aiStartTime;
				log.info("✅ AI response received in {}ms", aiDuration);
				
				// 2. Process AI response
				long processStartTime = System.currentTimeMillis();
				String respContent = aiResp.getContent();
				List<MenuMealLiteResponse> menuList = aiResp.getMenu();
				boolean isJson = (menuList != null && !menuList.isEmpty());

				// Update AI message: lưu kèm menuJson để FE có thể backfill/pagination hiển thị menu
				aiMsg.setContent(respContent);
				aiMsg.setStatus(MessageStatus.SENT);
				if (isJson) {
					try {
						aiMsg.setMenuJson(om.writeValueAsString(menuList));
					} catch (Exception ex) {
						aiMsg.setMenuJson(null);
					}
				} else {
					aiMsg.setMenuJson(null);
				}
				ChatMessage finalizedAiMsg = chatMessageRepo.saveAndFlush(aiMsg);

				// Update user message status
				userMsg.setStatus(MessageStatus.SENT);
				chatMessageRepo.saveAndFlush(userMsg);

				long processDuration = System.currentTimeMillis() - processStartTime;
				log.info("⚙️ Message processing completed in {}ms", processDuration);

				// 3. Final response
				ChatResponse resp = mapper.map(finalizedAiMsg, ChatResponse.class);
				resp.setSenderRole(SenderType.AI.name());
				if (isJson) {
					resp.setMenu(menuList);
				}

				resp.setConversationStatus(conv.getStatus().name());
				messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
				
				long totalDuration = System.currentTimeMillis() - startTime;
				log.info("🎯 AI processing completed in {}ms (AI: {}ms, Processing: {}ms) - Conversation: {}", 
						totalDuration, aiDuration, processDuration, conv.getId());

				return resp;
				
			} catch (Exception e) {
				long totalDuration = System.currentTimeMillis() - startTime;
				log.error("❌ AI processing failed after {}ms for conversation {}: {}", totalDuration, conv.getId(), e.getMessage(), e);
				
				// Cập nhật status của AI message thành FAILED nếu có lỗi
				aiMsg.setStatus(MessageStatus.FAILED);
				aiMsg.setContent("Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.");
				chatMessageRepo.saveAndFlush(aiMsg);
				
				// Emit message lỗi
				ChatResponse errorResp = mapper.map(aiMsg, ChatResponse.class);
				errorResp.setSenderRole(SenderType.AI.name());
				errorResp.setConversationStatus(conv.getStatus().name());
				messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), errorResp);
				
				return errorResp;
			}
		});
	}

	private ChatResponse handleCustomerMessage(Long actorId, ChatRequest request, Conversation conv) {
	    String content = request.getContent();

	    // 1. Các command đặc biệt - xử lý trong transaction riêng
	    if ("/meet_emp".equals(content)) {
	        return handleMeetEmpCommand(conv);
	    }

	    if ("/backtoAI".equals(content)) {
	        return handleBackToAICommand(conv);
	    }

	    // 2. Nếu hội thoại không ở trạng thái AI → gửi thẳng cho EMP
	    if (conv.getStatus() != ConversationStatus.AI) {
	        return handleEmployeeMessage(actorId, content, conv);
	    }

	    // 3. Lưu message PENDING trong transaction riêng biệt - ĐẢM BẢO LUÔN ĐƯỢC LƯU
	    PendingMessageResult pendingResult = saveCustomerPendingMessage(actorId, request, conv);
	    // Emit đã được thực hiện trong saveCustomerPendingMessage; không cần giữ biến trả về
	    ChatMessage userMsg = pendingResult.getUserMsg();
	    ChatMessage aiMsg = pendingResult.getAiMsg();

	    // 4. Lấy 20 tin nhắn để build context cho AI
	    List<ChatMessage> last20Msgs = chatMessageRepo.findTop10ByConversationOrderByTimestampDesc(conv);
	    Collections.reverse(last20Msgs);

	    // 5. Lấy health info
	    String healthInfoJson = "[]";
	    if (actorId != null) {
	        List<CustomerReference> refs = customerReferenceService.getCustomerReferencesByCustomerId(actorId);
	        try {
	            if (refs != null && !refs.isEmpty()) {
	                ObjectMapper objectMapper = new ObjectMapper();
	                objectMapper.registerModule(new JavaTimeModule());
	                healthInfoJson = objectMapper.writeValueAsString(refs);
	            }
	        } catch (JsonProcessingException e) {
	            log.warn("Không convert được health info sang JSON", e);
	        }
	    }


		// 6. Build context bằng rolling summary + recent + health info
		String context = chatSummaryService.buildContextForAi(conv.getId(), request.getContent().trim());
		if (actorId != null) {
			context = context + "\n<<<HEALTH_INFO>>>\n" + healthInfoJson + "\n<<<END_HEALTH_INFO>>>\n";
		}
		if (!isMenuIntent(request.getContent())) {
			context = context + "\nNote: Current user is not asking about menu. Do NOT call menu tool.\n";
		}
		
		// 7. Add context reset hint if conversation was recently returned from EMP
		context = context + "\n<<<CONVERSATION_CONTEXT>>>\n" +
				"IMPORTANT: This conversation is now handled by AI. Focus on menu consultation and nutrition advice. " +
				"Only call requestMeetEmp if user explicitly requests to speak with a human employee (Vietnamese: 'gặp nhân viên', 'nói chuyện với người thật' or English: 'meet employee', 'talk to human'). " +
				"Ignore any previous messages about meeting employees or system transitions. " +
				"Respond in the same language as the user's current message.\n" +
				"<<<END_CONVERSATION_CONTEXT>>>\n";

		// 8. Xử lý AI response trong transaction riêng biệt
		ChatResponse result = processAIResponse(context, request.getLang(), userMsg, aiMsg, conv);
		// 9. Non-blocking trigger summarize when window grows (best-effort)
		new Thread(() -> {
			try { chatSummaryService.summarizeIncrementally(conv.getId()); } catch (Exception ignored) {}
		}).start();
		return result;
	}
	// Helper: Kiểm tra intent menu
	private boolean isMenuIntent(String message) {
		if (message == null) return false;
		String lower = message.toLowerCase();
		return lower.contains("menu") || lower.contains("món") || lower.contains("giá") || lower.contains("calorie") || lower.contains("nguyên liệu") || lower.contains("khẩu phần") || lower.contains("loại món");
	}


	private ChatResponse handleEmployeeMessageFromEmployee(Long actorId, ChatRequest request, Conversation conv) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		return txTemplate.execute(status -> {
			Employee emp = employeeRepo.findById(actorId)
					.orElseThrow(() -> new EntityNotFoundException("Employee không tồn tại"));

			// Kiểm tra trạng thái trước khi update
			Conversation currentConv = conversationRepo.findById(conv.getId())
					.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
			
			if (currentConv.getStatus() != ConversationStatus.EMP || !emp.equals(currentConv.getEmployee())) {
				currentConv.setStatus(ConversationStatus.EMP);
				currentConv.setEmployee(emp);
				conversationRepo.save(currentConv);
			}
			
            EmpNotifyPayload payload = new EmpNotifyPayload(conv.getId(), ConversationStatus.EMP.name(), "EMP", LocalDateTime.now());
            messagingTemplate.convertAndSend("/topic/emp-notify", payload);

			ChatMessage empMsg = buildMessage(null, emp, conv, emp.getFirstName(), SenderType.EMP, false,
					request.getContent());
			empMsg.setStatus(MessageStatus.SENT);
			chatMessageRepo.save(empMsg); // Không dùng saveAndFlush

			ChatResponse resp = mapper.map(empMsg, ChatResponse.class);
			resp.setSenderRole(SenderType.EMP.name());
			resp.setConversationStatus(conv.getStatus().name());
			messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
			return resp;
		});
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
    private MenuMealsAiResponse callAi(String prompt, String lang, Long conversationId) {
	    String systemPrompt;
	    try {
	        systemPrompt = loadPrompt("PromtAIGreenKitchen.md");
	    } catch (IOException e) {
	        log.error("Không thể tải prompt từ file: " + e.getMessage());
	        systemPrompt = "Bạn là nhân viên tư vấn dinh dưỡng & CSKH của Green Kitchen...";
	    }

        // Inject guardrails: chỉ escalate khi có từ khóa rõ ràng; không escalate cho chào hỏi
        String augmentedUserPrompt = prompt + "\n\n[CRITICAL TOOL_CALL_RULES - READ CAREFULLY BEFORE CALLING TOOL]\n" +
                "🚨 IMPORTANT: ONLY call requestMeetEmp(conversationId) when user EXPLICITLY and SPECIFICALLY requests:\n" +
                "✅ ALLOWED (Vietnamese): 'gặp nhân viên', 'nói chuyện với người thật', 'kết nối nhân viên', 'gọi hotline', 'liên hệ hỗ trợ', 'tôi muốn gặp nhân viên', 'cần hỗ trợ từ người thật'\n" +
                "✅ ALLOWED (English): 'meet employee', 'talk to human', 'connect to employee', 'call hotline', 'contact support', 'human agent', 'support agent', 'I want to speak with a human', 'need human support'\n" +
                "❌ FORBIDDEN: 'hello', 'hi', 'chào', 'alo', 'test', 'có ai không', 'bạn có thể giúp không', 'can you help', 'tư vấn', 'hỏi', 'menu', 'món ăn', 'giá', 'calorie', 'food', 'nutrition', or ANY other questions\n" +
                "⚠️ IF NOT 100% SURE → ASK USER AGAIN instead of calling tool\n" +
                "🔢 conversationId=" + conversationId + " (required, cannot be null)\n" +
                "📝 Do not return Markdown/HTML when deciding to call tool\n" +
                "🎯 GOAL: Menu consultation and nutrition advice, NOT connecting to employees unless explicitly requested\n";

        return chatClient.prompt()
	            .system(systemPrompt)
	            .tools(menuTools)
                .user(augmentedUserPrompt)
	            .call()
	            .entity(new ParameterizedTypeReference<MenuMealsAiResponse>() {});
	    
	}


	private String loadPrompt(String fileName) throws IOException {
		ClassPathResource resource = new ClassPathResource("prompts/" + fileName);
		try (InputStream is = resource.getInputStream()) {
			return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
		}
	}

	@Override
	public void markCustomerMessagesAsRead(Long conversationId) {
		Conversation conv = conversationRepo.findById(conversationId)
				.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
		chatMessageRepo.markMessagesAsRead(conv, SenderType.CUSTOMER);
	}

	@Override
	@CacheEvict(value = "conversations", allEntries = true)
	public void claimConversationAsEmp(Long conversationId, Long employeeId) {
		// FIX: Optimistic Locking với retry mechanism để ngăn race condition
		int maxRetries = 3;
		int retryCount = 0;
		
		while (retryCount < maxRetries) {
			try {
				// 1. Load conversation với version check
				Conversation conv = conversationRepo.findById(conversationId)
						.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
				
				// 2. Kiểm tra trạng thái hiện tại
				if (conv.getStatus() == ConversationStatus.EMP && conv.getEmployee() != null) {
					if (conv.getEmployee().getId().equals(employeeId)) {
						// EMP đã claim rồi, không cần làm gì
						log.info("Conversation {} already claimed by employee {}", conversationId, employeeId);
						return;
					} else {
						// Đã được claim bởi EMP khác
						throw new ResponseStatusException(HttpStatus.CONFLICT, 
							"Conversation đã được claim bởi nhân viên khác");
					}
				}
				
				// 3. Validate employee
				Employee emp = employeeRepo.findById(employeeId)
						.orElseThrow(() -> new EntityNotFoundException("Employee không tồn tại"));
				
				// 4. Update với optimistic locking
				conv.setStatus(ConversationStatus.EMP);
				conv.setEmployee(emp);
				conversationRepo.saveAndFlush(conv); // Sẽ throw OptimisticLockingFailureException nếu version conflict
				
				// 5. Success - gửi notification
				EmpNotifyPayload payload = new EmpNotifyPayload(conv.getId(), ConversationStatus.EMP.name(), "EMP", LocalDateTime.now());
				messagingTemplate.convertAndSend("/topic/emp-notify", payload);
				
				log.info("Successfully claimed conversation {} by employee {} (attempt {})", 
					conversationId, employeeId, retryCount + 1);
				return;
				
			} catch (OptimisticLockingFailureException e) {
				retryCount++;
				log.warn("Optimistic locking failure for conversation {} (attempt {}/{}): {}", 
					conversationId, retryCount, maxRetries, e.getMessage());
				
				if (retryCount >= maxRetries) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, 
						"Không thể claim conversation do xung đột. Vui lòng thử lại.");
				}
				
				// Exponential backoff: 100ms, 200ms, 400ms
				try {
					Thread.sleep(100 * (1L << (retryCount - 1)));
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operation interrupted");
				}
			}
		}
	}

	@Override
	@CacheEvict(value = "conversations", allEntries = true)
	public void releaseConversationToAI(Long conversationId) {
		// FIX: Optimistic Locking cho release operation
		int maxRetries = 3;
		int retryCount = 0;
		
		while (retryCount < maxRetries) {
			try {
				Conversation conv = conversationRepo.findById(conversationId)
						.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
				
				// Kiểm tra trạng thái hiện tại
				if (conv.getStatus() == ConversationStatus.AI && conv.getEmployee() == null) {
					// Đã release rồi, không cần làm gì
					log.info("Conversation {} already released to AI", conversationId);
					return;
				}
				
				conv.setStatus(ConversationStatus.AI);
				conv.setEmployee(null);
				conversationRepo.saveAndFlush(conv);
				
				EmpNotifyPayload payload = new EmpNotifyPayload(conv.getId(), ConversationStatus.AI.name(), "EMP", LocalDateTime.now());
				messagingTemplate.convertAndSend("/topic/emp-notify", payload);
				
				log.info("Successfully released conversation {} to AI (attempt {})", 
					conversationId, retryCount + 1);
				return;
				
			} catch (OptimisticLockingFailureException e) {
				retryCount++;
				log.warn("Optimistic locking failure for release conversation {} (attempt {}/{}): {}", 
					conversationId, retryCount, maxRetries, e.getMessage());
				
				if (retryCount >= maxRetries) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, 
						"Không thể release conversation do xung đột. Vui lòng thử lại.");
				}
				
				// Exponential backoff
				try {
					Thread.sleep(100 * (1L << (retryCount - 1)));
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operation interrupted");
				}
			}
		}
	}
}
