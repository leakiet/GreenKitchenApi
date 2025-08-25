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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.dtos.MenuMealResponse;
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

	ObjectMapper om = new ObjectMapper();

	@Override
	@Transactional
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

	// Tạo hoặc lấy conversation trong transaction chính
	private Conversation createOrGetConversation(Long actorId, SenderType senderType, Long convId) {
		if (convId != null) {
			return conversationRepo.findById(convId)
					.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
		}
		
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
	}

	// Transaction riêng cho command /meet_emp
	@Transactional(timeout = 30) // Tăng từ 15 lên 30 giây
	private ChatResponse handleMeetEmpCommand(Conversation conv) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		try {
			return txTemplate.execute(status -> {
				// Kiểm tra trạng thái trước khi update để tránh update trùng
				Conversation currentConv = conversationRepo.findById(conv.getId())
						.orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
				
				if (currentConv.getStatus() != ConversationStatus.WAITING_EMP) {
					currentConv.setStatus(ConversationStatus.WAITING_EMP);
					conversationRepo.save(currentConv);
				}
				
				messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
				return new ChatResponse(null, conv.getId(), SenderType.SYSTEM.name(), "SYSTEM",
						"Yêu cầu đã gửi, vui lòng chờ nhân viên.", null, LocalDateTime.now(), MessageStatus.SENT);
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
				
				messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
				return new ChatResponse(null, conv.getId(), SenderType.SYSTEM.name(), "SYSTEM",
						"Chuyển về AI thành công.", null, LocalDateTime.now(), MessageStatus.SENT);
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
			messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
			messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());
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
			messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), userResp);

			ChatResponse aiPendingResp = mapper.map(aiMsg, ChatResponse.class);
			aiPendingResp.setSenderRole(SenderType.AI.name());
			messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), aiPendingResp);

			// Trả về wrapper object chứa cả 2 message
			return new PendingMessageResult(userResp, aiPendingResp, userMsg, aiMsg);
		});
	}

	// Transaction riêng cho việc xử lý AI và cập nhật message
	@Transactional(timeout = 45) // 45 giây timeout cho AI processing
	private ChatResponse processAIResponse(String context, String lang, ChatMessage userMsg, ChatMessage aiMsg, Conversation conv) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		return txTemplate.execute(status -> {
			try {
				// 4. Gọi AI → đã trả về DTO (MenuMealsAiResponse)
				MenuMealsAiResponse aiResp = callAi(context, lang);
				String respContent = aiResp.getContent();
				List<MenuMealResponse> menuList = aiResp.getMenu();
				boolean isJson = (menuList != null && !menuList.isEmpty());

				// 5. Cập nhật message AI
				aiMsg.setContent(respContent);
				if (isJson) {
					try {
						aiMsg.setMenuJson(om.writeValueAsString(menuList));
					} catch (JsonProcessingException e) {
						log.warn("Không lưu được menu JSON: {}", e.getMessage());
					}
				}
				aiMsg.setStatus(MessageStatus.SENT);
				ChatMessage finalizedAiMsg = chatMessageRepo.saveAndFlush(aiMsg);

				// Cập nhật status của user message thành SENT
				userMsg.setStatus(MessageStatus.SENT);
				chatMessageRepo.saveAndFlush(userMsg);

				// 6. Emit kết quả cuối
				ChatResponse resp = mapper.map(finalizedAiMsg, ChatResponse.class);
				resp.setSenderRole(SenderType.AI.name());
				if (isJson) {
					resp.setMenu(menuList);
				}

				messagingTemplate.convertAndSend("/topic/conversations/" + conv.getId(), resp);
				return resp;
				
			} catch (Exception e) {
				log.error("Lỗi khi xử lý AI response cho conversation {}: {}", conv.getId(), e.getMessage(), e);
				
				// Cập nhật status của AI message thành FAILED nếu có lỗi
				aiMsg.setStatus(MessageStatus.FAILED);
				aiMsg.setContent("Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.");
				chatMessageRepo.saveAndFlush(aiMsg);
				
				// Emit message lỗi
				ChatResponse errorResp = mapper.map(aiMsg, ChatResponse.class);
				errorResp.setSenderRole(SenderType.AI.name());
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
	    ChatResponse userResp = pendingResult.getUserResp();
	    ChatResponse aiPendingResp = pendingResult.getAiPendingResp();
	    ChatMessage userMsg = pendingResult.getUserMsg();
	    ChatMessage aiMsg = pendingResult.getAiMsg();

	    // 4. Lấy 20 tin nhắn để build context cho AI
	    List<ChatMessage> last20Msgs = chatMessageRepo.findTop20ByConversationOrderByTimestampDesc(conv);
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

	    // 6. Build context cho AI
	    StringBuilder sb = new StringBuilder();
	    sb.append("<<<HISTORY>>>\n");
	    for (ChatMessage msg : last20Msgs) {
	        String role = switch (msg.getSenderType().name()) {
	            case "CUSTOMER" -> "user";
	            case "AI" -> "assistant";
	            case "EMP" -> "employee";
	            default -> "other";
	        };
	        sb.append(role).append("|").append(msg.getSenderName()).append("| ")
	                .append(msg.getContent().replace("\n", " ").trim()).append("\n");
	    }
	    sb.append("<<<END_HISTORY>>>\n\n");

	    if (actorId != null) {
	        sb.append("<<<HEALTH_INFO>>>\n")
	          .append(healthInfoJson)
	          .append("\n<<<END_HEALTH_INFO>>>\n\n");
	    }

	    sb.append("<<<CURRENT_USER_MESSAGE>>>\n").append(request.getContent().trim())
	            .append("\n<<<END_CURRENT_USER_MESSAGE>>>\n");

	    String context = sb.toString();

	    // 7. Xử lý AI response trong transaction riêng biệt
	    return processAIResponse(context, request.getLang(), userMsg, aiMsg, conv);
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
			
			messagingTemplate.convertAndSend("/topic/emp-notify", conv.getId());

			ChatMessage empMsg = buildMessage(null, emp, conv, emp.getFirstName(), SenderType.EMP, false,
					request.getContent());
			empMsg.setStatus(MessageStatus.SENT);
			chatMessageRepo.save(empMsg); // Không dùng saveAndFlush

			ChatResponse resp = mapper.map(empMsg, ChatResponse.class);
			resp.setSenderRole(SenderType.EMP.name());
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
	private MenuMealsAiResponse callAi(String prompt, String lang) {
	    String systemPrompt;
	    try {
	        systemPrompt = loadPrompt("PromtAIGreenKitchen.md");
	    } catch (IOException e) {
	        log.error("Không thể tải prompt từ file: " + e.getMessage());
	        systemPrompt = "Bạn là nhân viên tư vấn dinh dưỡng & CSKH của Green Kitchen...";
	    }

	    return chatClient.prompt()
	            .system(systemPrompt)
	            .tools(menuTools)
	            .user(prompt)
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
