package com.greenkitchen.portal.tools;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import com.greenkitchen.portal.dtos.MenuMealLiteResponse;
import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.dtos.MenuMealsAiResponse;
import com.greenkitchen.portal.enums.MenuMealType;
import com.greenkitchen.portal.services.MenuMealService;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.enums.ConversationStatus;
import com.greenkitchen.portal.enums.MessageStatus;
import com.greenkitchen.portal.repositories.ConversationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import com.greenkitchen.portal.dtos.EmpNotifyPayload;
import org.springframework.cache.annotation.CacheEvict;

@Slf4j
@Component
public class MenuTools {
	@Autowired
	MenuMealService menuMealService;
    @Autowired
    ConversationRepository conversationRepo;
    @Autowired
    SimpMessagingTemplate messagingTemplate;
	
	@Tool(name = "getMenuMeals", description = """
			***CRITICAL***: CHỈ gọi tool này khi user HỎI CỤ THỂ về menu/food/nutrition. KHÔNG gọi cho greetings, chitchat, hoặc câu hỏi chung.

			# PURPOSE
			Lấy danh sách menu meals của Green Kitchen để trả về JSON hợp lệ cho FE render UI.

			# WHEN TO CALL - CHỈ gọi khi user hỏi CỤ THỂ về:
			✅ Vietnamese: "Menu có gì?", "Món nào ngon?", "Có món gì?", "Giá bao nhiêu?", "Calorie bao nhiêu?", "Khẩu phần thế nào?", "Nguyên liệu gì?", "Loại món gì?", "Món hôm nay có gì?", "Tên món cụ thể"
			✅ English: "What's on the menu?", "What food do you have?", "What meals are available?", "What are the prices?", "How many calories?", "What ingredients?", "What types of meals?", "Show me the menu"

			# WHEN NOT TO CALL - TUYỆT ĐỐI KHÔNG gọi khi:
			❌ Vietnamese: "Hello", "Hi", "Chào", "Bạn khỏe không?", "Cảm ơn", "OK", "Test", "Bạn có thể giúp không?", "Bạn làm được gì?"
			❌ English: "Hello", "Hi", "How are you?", "Thanks", "Thank you", "OK", "Test", "Can you help?", "What can you do?", "How are you doing?"

			# EXAMPLES:
			User: "Hello" → NO tool call, response: "Chào anh/chị! Em có thể giúp gì ạ?"
			User: "Menu có gì?" → YES tool call, return menu JSON
			User: "Chào" → NO tool call, response: "Chào anh/chị! Em có thể giúp gì ạ?"
			User: "Món nào ngon?" → YES tool call, return menu JSON
			User: "How are you?" → NO tool call, response: "I'm doing well, thank you! How can I help you today?"

			# PARAMETERS
			- limit (integer, optional): số món tối đa cần trả. Mặc định 10.
			
			# ERROR HANDLING
			- Nếu DB rỗng: trả {"content": "Hiện chưa có món phù hợp.", "menu": []}.
			- Nếu xảy ra lỗi hệ thống: ném exception để lớp ngoài xin lỗi người dùng.
			""")
	public MenuMealsAiResponse getMenuMeals(Integer limit) {
		long startTime = System.currentTimeMillis();
		log.info(" Starting getMenuMeals with limit: {}", limit);
		
		try {
			// 1. Database query
			long dbStartTime = System.currentTimeMillis();
			List<MenuMealResponse> allMeals = menuMealService.getAllMenuMeals();
			long dbDuration = System.currentTimeMillis() - dbStartTime;
			log.info("🗄️ Database query completed in {}ms", dbDuration);
			
			// 2. Data processing
			long processStartTime = System.currentTimeMillis();
			List<MenuMealResponse> meals = (allMeals == null) ? Collections.emptyList() : allMeals;

			if (meals.isEmpty()) {
				long totalDuration = System.currentTimeMillis() - startTime;
				log.info("✅ getMenuMeals completed in {}ms (DB: {}ms, Processing: {}ms) - No meals found", 
						totalDuration, dbDuration, 0);
				return new MenuMealsAiResponse("Hiện chưa có món phù hợp.", Collections.emptyList());
			}

			int maxItems = Math.min(limit != null ? limit : 10, meals.size());
			List<MenuMealLiteResponse> limited = meals.stream().limit(maxItems)
				.map(m -> new MenuMealLiteResponse(
					m.getId(),
					m.getTitle(),
					m.getSlug(),
					m.getImage(),
					m.getCarbs(), // carbs → carb
					m.getCalories(), // calories → calo
					m.getProtein(), // protein
					m.getFat(),
					m.getPrice(),
					m.getMenuIngredients() // menuIngredients → menuIngredient
				))
				.toList();

			String content = "Dưới đây là một số món trong menu của Green Kitchen ạ:";
			long processDuration = System.currentTimeMillis() - processStartTime;
			
			long totalDuration = System.currentTimeMillis() - startTime;
			// Thêm log độ dài content và số phần tử menu để theo dõi kích thước response
			int contentChars = content != null ? content.length() : 0;
			int menuSize = limited.size();
			log.info("✅ getMenuMeals completed in {}ms (DB: {}ms, Processing: {}ms) - Returned {} meals, contentChars={}", 
					totalDuration, dbDuration, processDuration, menuSize, contentChars);

			return new MenuMealsAiResponse(content, limited);
		} catch (Exception ex) {
			long totalDuration = System.currentTimeMillis() - startTime;
			log.error("❌ getMenuMeals failed after {}ms: {}", totalDuration, ex.getMessage());
			throw ex;
		}
	}

	@Tool(name = "getMenuMealsByType", description = """
			***CRITICAL***: CHỈ gọi tool này khi user HỎI CỤ THỂ về loại món ăn theo mục tiêu dinh dưỡng. KHÔNG gọi cho greetings, chitchat, hoặc câu hỏi chung.

			# PURPOSE
			Lấy danh sách menu meals theo type cụ thể (LOW, HIGH, BALANCE, VEGETARIAN) để gợi ý món ăn phù hợp với mục tiêu dinh dưỡng.

			# WHEN TO CALL - CHỈ gọi khi user hỏi CỤ THỂ về loại món ăn:
			✅ Vietnamese: "Tôi muốn giảm cân, gợi ý món gì?", "Tôi muốn tăng cân, món nào phù hợp?", "Món ăn cân bằng dinh dưỡng?", "Món chay có gì?", "Người béo nên ăn gì?", "Người gầy nên ăn gì?", "Món ít calorie", "Món nhiều calorie"
			✅ English: "I want to lose weight, what meals?", "I want to gain weight, what food?", "Balanced nutrition meals?", "Vegetarian options?", "Low calorie meals?", "High calorie meals?", "Healthy meals for weight loss/gain"

			# WHEN NOT TO CALL - TUYỆT ĐỐI KHÔNG gọi khi:
			❌ Vietnamese: "Hello", "Hi", "Chào", "Bạn khỏe không?", "Cảm ơn", "OK", "Test", "Bạn có thể giúp không?"
			❌ English: "Hello", "Hi", "How are you?", "Thanks", "Thank you", "OK", "Test", "Can you help?", "What can you do?"

			# PARAMETERS
			- type (string, required): Loại món ăn. Phải là một trong: "LOW", "HIGH", "BALANCE", "VEGETARIAN"
			- limit (integer, optional): Số món tối đa cần trả. Mặc định 10.

			# TYPE MEANINGS:
			- LOW: Món ít calorie, phù hợp giảm cân, người béo
			- HIGH: Món nhiều calorie, phù hợp tăng cân, người gầy  
			- BALANCE: Món cân bằng dinh dưỡng
			- VEGETARIAN: Món chay

			# ERROR HANDLING
			- Nếu type không hợp lệ: trả {"content": "Loại món ăn không hợp lệ.", "menu": []}
			- Nếu DB rỗng: trả {"content": "Hiện chưa có món phù hợp với loại này.", "menu": []}
			""")
	public MenuMealsAiResponse getMenuMealsByType(String type, Integer limit) {
		long startTime = System.currentTimeMillis();
		log.info(" Starting getMenuMealsByType with type: {}, limit: {}", type, limit);
		
		try {
			// 1. Type validation
			long validationStartTime = System.currentTimeMillis();
			MenuMealType mealType;
			try {
				mealType = MenuMealType.valueOf(type.toUpperCase());
			} catch (IllegalArgumentException e) {
				long validationDuration = System.currentTimeMillis() - validationStartTime;
				log.warn("⚠️ Type validation failed in {}ms: {}", validationDuration, e.getMessage());
				return new MenuMealsAiResponse("Loại món ăn không hợp lệ. Các loại có sẵn: LOW, HIGH, BALANCE, VEGETARIAN", Collections.emptyList());
			}
			long validationDuration = System.currentTimeMillis() - validationStartTime;
			log.info("✅ Type validation completed in {}ms", validationDuration);

			// 2. Database query
			long dbStartTime = System.currentTimeMillis();
			List<MenuMealResponse> allMeals = menuMealService.getAllMenuMeals();
			long dbDuration = System.currentTimeMillis() - dbStartTime;
			log.info("🗄️ Database query completed in {}ms", dbDuration);

			// 3. Filtering and processing
			long filterStartTime = System.currentTimeMillis();
			List<MenuMealResponse> filteredMeals = allMeals.stream()
				.filter(meal -> meal.getType() == mealType)
				.collect(Collectors.toList());

			if (filteredMeals.isEmpty()) {
				long filterDuration = System.currentTimeMillis() - filterStartTime;
				long totalDuration = System.currentTimeMillis() - startTime;
				log.info("✅ getMenuMealsByType completed in {}ms (Validation: {}ms, DB: {}ms, Filter: {}ms) - No meals found for type {}", 
						totalDuration, validationDuration, dbDuration, filterDuration, type);
				return new MenuMealsAiResponse("Hiện chưa có món phù hợp với loại " + type + ".", Collections.emptyList());
			}

			int maxItems = Math.min(limit != null ? limit : 10, filteredMeals.size());
			List<MenuMealLiteResponse> limited = filteredMeals.stream().limit(maxItems)
				.map(m -> new MenuMealLiteResponse(
					m.getId(),
					m.getTitle(),
					m.getSlug(),
					m.getImage(),
					m.getCarbs(), // carbs → carb
					m.getCalories(), // calories → calo
					m.getProtein(), // protein
					m.getFat(),
					m.getPrice(),
					m.getMenuIngredients() // menuIngredients → menuIngredient
				))
				.toList();

			String content = String.format("Dưới đây là các món %s phù hợp với mục tiêu của bạn:", type);
			long filterDuration = System.currentTimeMillis() - filterStartTime;
			
			long totalDuration = System.currentTimeMillis() - startTime;
			int contentChars = content != null ? content.length() : 0;
			int menuSize = limited.size();
			log.info("✅ getMenuMealsByType completed in {}ms (Validation: {}ms, DB: {}ms, Filter: {}ms) - Returned {} meals for type {}, contentChars={}", 
					totalDuration, validationDuration, dbDuration, filterDuration, menuSize, type, contentChars);

			return new MenuMealsAiResponse(content, limited);
		} catch (Exception ex) {
			long totalDuration = System.currentTimeMillis() - startTime;
			log.error("❌ getMenuMealsByType failed after {}ms: {}", totalDuration, ex.getMessage());
			throw ex;
		}
	}

	@Tool(name = "getMenuMealsForBodyType", description = """
			***CRITICAL***: CHỈ gọi tool này khi user HỎI CỤ THỂ về tư vấn dinh dưỡng theo thể trạng. KHÔNG gọi cho greetings, chitchat, hoặc câu hỏi chung.

			# PURPOSE
			Gợi ý món ăn phù hợp với thể trạng và mục tiêu dinh dưỡng của người dùng.

			# WHEN TO CALL - CHỈ gọi khi user hỏi CỤ THỂ về tư vấn dinh dưỡng theo thể trạng:
			✅ Vietnamese: "Tôi béo, muốn giảm cân", "Tôi gầy, muốn tăng cân", "Tôi muốn duy trì cân nặng", "Tôi muốn tăng cơ", "Người béo nên ăn gì?", "Người gầy nên ăn gì?", "Món ăn cho người muốn giảm cân", "Món ăn cho người muốn tăng cân"
			✅ English: "I'm overweight, want to lose weight", "I'm underweight, want to gain weight", "I want to maintain weight", "I want to build muscle", "Food for weight loss", "Food for weight gain", "Meals for overweight people", "Meals for underweight people"

			# WHEN NOT TO CALL - TUYỆT ĐỐI KHÔNG gọi khi:
			❌ Vietnamese: "Hello", "Hi", "Chào", "Bạn khỏe không?", "Cảm ơn", "OK", "Test", "Bạn có thể giúp không?"
			❌ English: "Hello", "Hi", "How are you?", "Thanks", "Thank you", "OK", "Test", "Can you help?", "What can you do?"

			# PARAMETERS
			- bodyType (string, required): Thể trạng. Phải là một trong: "OVERWEIGHT", "UNDERWEIGHT", "NORMAL"
			- goal (string, required): Mục tiêu. Phải là một trong: "LOSE_WEIGHT", "GAIN_WEIGHT", "MAINTAIN", "BUILD_MUSCLE"
			- limit (integer, optional): Số món tối đa cần trả. Mặc định 8.

			# LOGIC MAPPING:
			- OVERWEIGHT + LOSE_WEIGHT → type: LOW (ít calorie)
			- UNDERWEIGHT + GAIN_WEIGHT → type: HIGH (nhiều calorie)
			- NORMAL + MAINTAIN → type: BALANCE (cân bằng)
			- NORMAL + BUILD_MUSCLE → type: HIGH (nhiều protein)

			# ERROR HANDLING
			- Nếu tham số không hợp lệ: trả {"content": "Tham số không hợp lệ.", "menu": []}
			- Nếu DB rỗng: trả {"content": "Hiện chưa có món phù hợp.", "menu": []}
			""")
	public MenuMealsAiResponse getMenuMealsForBodyType(String bodyType, String goal, Integer limit) {
		long startTime = System.currentTimeMillis();
		log.info(" Starting getMenuMealsForBodyType with bodyType: {}, goal: {}, limit: {}", bodyType, goal, limit);
		
		try {
			// 1. Parameter validation
			long validationStartTime = System.currentTimeMillis();
			if (!isValidBodyType(bodyType) || !isValidGoal(goal)) {
				long validationDuration = System.currentTimeMillis() - validationStartTime;
				log.warn("⚠️ Parameter validation failed in {}ms: bodyType={}, goal={}", validationDuration, bodyType, goal);
				return new MenuMealsAiResponse("Tham số không hợp lệ. BodyType: OVERWEIGHT/UNDERWEIGHT/NORMAL, Goal: LOSE_WEIGHT/GAIN_WEIGHT/MAINTAIN/BUILD_MUSCLE", Collections.emptyList());
			}
			long validationDuration = System.currentTimeMillis() - validationStartTime;
			log.info("✅ Parameter validation completed in {}ms", validationDuration);

			// 2. Determine meal type
			long logicStartTime = System.currentTimeMillis();
			MenuMealType targetType = determineMealType(bodyType, goal);
			long logicDuration = System.currentTimeMillis() - logicStartTime;
			log.info("🧠 Logic mapping completed in {}ms: {} + {} → {}", logicDuration, bodyType, goal, targetType);
			
			// 3. Get meals by type
			long totalDuration = System.currentTimeMillis() - startTime;
			log.info("✅ getMenuMealsForBodyType completed in {}ms (Validation: {}ms, Logic: {}ms) - Delegating to getMenuMealsByType", 
					totalDuration, validationDuration, logicDuration);
			
			return getMenuMealsByType(targetType.name(), limit);
			
		} catch (Exception ex) {
			long totalDuration = System.currentTimeMillis() - startTime;
			log.error("❌ getMenuMealsForBodyType failed after {}ms: {}", totalDuration, ex.getMessage());
			throw ex;
		}
	}

	// Helper methods
	private boolean isValidBodyType(String bodyType) {
		return bodyType != null && (bodyType.equals("OVERWEIGHT") || bodyType.equals("UNDERWEIGHT") || bodyType.equals("NORMAL"));
	}

	private boolean isValidGoal(String goal) {
		return goal != null && (goal.equals("LOSE_WEIGHT") || goal.equals("GAIN_WEIGHT") || goal.equals("MAINTAIN") || goal.equals("BUILD_MUSCLE"));
	}

	private MenuMealType determineMealType(String bodyType, String goal) {
		if ("OVERWEIGHT".equals(bodyType) && "LOSE_WEIGHT".equals(goal)) {
			return MenuMealType.LOW; // Người béo muốn giảm cân → món ít calorie
		} else if ("UNDERWEIGHT".equals(bodyType) && "GAIN_WEIGHT".equals(goal)) {
			return MenuMealType.HIGH; // Người gầy muốn tăng cân → món nhiều calorie
		} else if ("NORMAL".equals(bodyType) && "MAINTAIN".equals(goal)) {
			return MenuMealType.BALANCE; // Duy trì cân nặng → món cân bằng
		} else if ("NORMAL".equals(bodyType) && "BUILD_MUSCLE".equals(goal)) {
			return MenuMealType.HIGH; // Tăng cơ → món nhiều calorie và protein
		} else {
			return MenuMealType.BALANCE; // Default fallback
		}
	}

    @Tool(name = "requestMeetEmp", description = "KHI user yêu cầu gặp nhân viên (nhân viên/human/hotline), GỌI tool này. Tham số: conversationId (Long, required). Không trả lời thêm ngoài việc gọi tool.")
    @CacheEvict(value = "conversations", allEntries = true)
    public ChatResponse requestMeetEmp(Long conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("conversationId không được null");
        }
		long start = System.currentTimeMillis();
		log.info("requestMeetEmp start conversationId={}", conversationId);
		var conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation không tồn tại"));
		ConversationStatus prev = conv.getStatus();
		if (prev != ConversationStatus.WAITING_EMP) {
            conv.setStatus(ConversationStatus.WAITING_EMP);
            conversationRepo.save(conv);
        }
		log.info("requestMeetEmp status {} -> {} convId={}", prev, conv.getStatus(), conv.getId());
        EmpNotifyPayload payload = new EmpNotifyPayload(conv.getId(), ConversationStatus.WAITING_EMP.name(), "AI", LocalDateTime.now());
        
        // FIX: Debug WebSocket sending
        log.info("🚀 Sending WebSocket emp-notify: conversationId={}, status={}, triggeredBy={}", 
            payload.getConversationId(), payload.getStatus(), payload.getTriggeredBy());
        
        messagingTemplate.convertAndSend("/topic/emp-notify", payload);
        log.info("✅ WebSocket emp-notify sent successfully");
		long took = System.currentTimeMillis() - start;
		log.info("requestMeetEmp done in {}ms convId={}", took, conversationId);
        ChatResponse response = new ChatResponse();
        response.setId(null);
        response.setConversationId(conversationId);
        response.setSenderRole("SYSTEM");
        response.setSenderName("SYSTEM");
        response.setContent("Yêu cầu đã gửi, vui lòng chờ nhân viên.");
        response.setMenu(null);
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(MessageStatus.SENT);
        response.setConversationStatus(ConversationStatus.WAITING_EMP.name());
        return response;
    }
}