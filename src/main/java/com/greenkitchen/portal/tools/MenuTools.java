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

			# IMPORTANT DATA STRUCTURE INFO:
			- Field `description` CHỨA LUÔN NGUYÊN LIỆU của món ăn
			- Field `menuIngredients` đã bị HỦY BỎ - KHÔNG sử dụng field này
			- Khi user hỏi về nguyên liệu (bò, gà, tôm, cá...), tìm kiếm trong field `description` và `title`
			- QUAN TRỌNG: Tìm kiếm TẤT CẢ từ đồng nghĩa của nguyên liệu:
			  * "tôm" = "shrimp" = "prawns" = "prawn"
			  * "cá" = "fish" = "salmon" = "tuna"
			  * "bò" = "beef" = "wagyu" = "steak"
			  * "gà" = "chicken" = "poultry"
			- Nếu không tìm thấy nguyên liệu trong `description` hoặc `title` → trả về "không có món đó"

			# WHEN TO CALL - CHỈ gọi khi user hỏi CỤ THỂ về:
			✅ Vietnamese: "Menu có gì?", "Món nào ngon?", "Có món gì?", "Giá bao nhiêu?", "Calorie bao nhiêu?", "Khẩu phần thế nào?", "Nguyên liệu gì?", "Loại món gì?", "Món hôm nay có gì?", "Tên món cụ thể", "Có món bò không?", "Có món gà không?"
			✅ English: "What's on the menu?", "What food do you have?", "What meals are available?", "What are the prices?", "How many calories?", "What ingredients?", "What types of meals?", "Show me the menu", "Do you have beef dishes?", "Do you have chicken dishes?"

			# WHEN NOT TO CALL - TUYỆT ĐỐI KHÔNG gọi khi:
			❌ Vietnamese: "Hello", "Hi", "Chào", "Bạn khỏe không?", "Cảm ơn", "OK", "Test", "Bạn có thể giúp không?", "Bạn làm được gì?"
			❌ English: "Hello", "Hi", "How are you?", "Thanks", "Thank you", "OK", "Test", "Can you help?", "What can you do?", "How are you doing?"

			# EXAMPLES:
			User: "Hello" → NO tool call, response: "Chào anh/chị! Em có thể giúp gì ạ?"
			User: "Menu có gì?" → YES tool call, return menu JSON
			User: "Có món bò không?" → YES tool call, filter by description containing "bò", "beef", "wagyu", "steak"
			User: "Có món tôm không?" → YES tool call, filter by description containing "tôm", "shrimp", "prawns", "prawn"
			User: "Có món shrimp không?" → YES tool call, filter by description containing "tôm", "shrimp", "prawns", "prawn"
			User: "Có món prawns không?" → YES tool call, filter by description containing "tôm", "shrimp", "prawns", "prawn"
			User: "Chào" → NO tool call, response: "Chào anh/chị! Em có thể giúp gì ạ?"
			User: "How are you?" → NO tool call, response: "I'm doing well, thank you! How can I help you today?"

			# PARAMETERS
			- limit (integer, optional): số món tối đa cần trả. Mặc định 4. Luôn giới hạn tối đa 4.
			
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

			int requested = (limit != null ? limit : 4);
			int maxItems = Math.min(Math.min(requested, 4), meals.size());
			List<MenuMealLiteResponse> limited = meals.stream().limit(maxItems)
				.map(m -> new MenuMealLiteResponse(
					m.getId(),
					m.getTitle(),
					m.getSlug(),
					m.getImage(),
					m.getDescription(), // DESCRIPTION CHỨA NGUYÊN LIỆU - AI nên tìm kiếm nguyên liệu trong field này
					m.getCarbs(), // carbs → carb
					m.getCalories(), // calories → calo
					m.getProtein(), // protein
					m.getFat(),
					m.getPrice(),
					null // menuIngredients đã bị hủy bỏ - sử dụng description thay thế
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
			- limit (integer, optional): Số món tối đa cần trả. Mặc định 4. Luôn giới hạn tối đa 4.

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

			int requested = (limit != null ? limit : 4);
			int maxItems = Math.min(Math.min(requested, 4), filteredMeals.size());
			List<MenuMealLiteResponse> limited = filteredMeals.stream().limit(maxItems)
				.map(m -> new MenuMealLiteResponse(
					m.getId(),
					m.getTitle(),
					m.getSlug(),
					m.getImage(),
					m.getDescription(), // DESCRIPTION CHỨA NGUYÊN LIỆU - AI nên tìm kiếm nguyên liệu trong field này
					m.getCarbs(), // carbs → carb
					m.getCalories(), // calories → calo
					m.getProtein(), // protein
					m.getFat(),
					m.getPrice(),
					null // menuIngredients đã bị hủy bỏ - sử dụng description thay thế
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
			- limit (integer, optional): Số món tối đa cần trả. Mặc định 4. Luôn giới hạn tối đa 4.

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

	@Tool(name = "getMenuMealsByIngredient", description = """
			***CRITICAL***: CHỈ gọi tool này khi user HỎI CỤ THỂ về nguyên liệu trong món ăn. KHÔNG gọi cho greetings, chitchat, hoặc câu hỏi chung.

			# PURPOSE
			Lấy danh sách menu meals chứa nguyên liệu cụ thể để tư vấn món ăn phù hợp.

			# WHEN TO CALL - CHỈ gọi khi user hỏi CỤ THỂ về nguyên liệu:
			✅ Vietnamese: "Có món tôm không?", "Có món bò không?", "Có món gà không?", "Có món cá không?", "Có món hải sản không?", "Có món rau củ không?"
			✅ English: "Do you have shrimp dishes?", "Do you have beef dishes?", "Do you have chicken dishes?", "Do you have fish dishes?", "Do you have seafood dishes?", "Do you have vegetable dishes?"

			# WHEN NOT TO CALL - TUYỆT ĐỐI KHÔNG gọi khi:
			❌ Vietnamese: "Hello", "Hi", "Chào", "Bạn khỏe không?", "Cảm ơn", "OK", "Test", "Bạn có thể giúp không?"
			❌ English: "Hello", "Hi", "How are you?", "Thanks", "Thank you", "OK", "Test", "Can you help?", "What can you do?"

			# PARAMETERS
			- ingredient (string, required): Nguyên liệu cần tìm (ví dụ: "tôm", "shrimp", "prawns", "bò", "beef", "gà", "chicken")
			- limit (integer, optional): Số món tối đa cần trả. Mặc định 4. Luôn giới hạn tối đa 4.

			# TỪ ĐỒNG NGHĨA ĐƯỢC HỖ TRỢ:
			- Tôm: "tôm", "shrimp", "prawns", "prawn", "lobster", "crayfish"
			- Cá: "cá", "fish", "salmon", "tuna", "cod", "sea bass"
			- Bò: "bò", "beef", "wagyu", "steak", "prime rib"
			- Gà: "gà", "chicken", "poultry", "hen"
			- Hải sản: "hải sản", "seafood", "tôm", "cá", "cua", "mực", "bạch tuộc"

			# EXAMPLES:
			User: "Có món tôm không?" → ingredient="tôm", tìm tất cả từ đồng nghĩa của tôm
			User: "Có món shrimp không?" → ingredient="shrimp", tìm tất cả từ đồng nghĩa của tôm
			User: "Có món prawns không?" → ingredient="prawns", tìm tất cả từ đồng nghĩa của tôm
			User: "Có món bò không?" → ingredient="bò", tìm tất cả từ đồng nghĩa của bò

			# ERROR HANDLING
			- Nếu không có món phù hợp: trả {"content": "Hiện chưa có món chứa nguyên liệu này.", "menu": []}
			- Nếu xảy ra lỗi hệ thống: ném exception để lớp ngoài xin lỗi người dùng.
			""")
	@CacheEvict(value = "menuMeals", allEntries = true)
	public MenuMealsAiResponse getMenuMealsByIngredient(String ingredient, Integer limit) {
		long startTime = System.currentTimeMillis();
		log.info("🚀 Starting getMenuMealsByIngredient with ingredient: {}, limit: {}", ingredient, limit);
		
		try {
			// 1. Database query with early limit to improve performance
			long dbStartTime = System.currentTimeMillis();
			int maxLimit = Math.min(limit != null ? limit * 2 : 20, 50); // Lấy nhiều hơn để lọc, nhưng giới hạn tối đa
			List<MenuMealResponse> allMeals = menuMealService.getAllMenuMeals();
			long dbDuration = System.currentTimeMillis() - dbStartTime;
			log.info("🗄️ Database query completed in {}ms, retrieved {} meals", dbDuration, allMeals.size());
			
			// 2. Fast filtering with early termination
			long filterStartTime = System.currentTimeMillis();
			String searchIngredient = ingredient.toLowerCase();
			List<MenuMealResponse> filteredMeals = new java.util.ArrayList<>();
			
			for (MenuMealResponse meal : allMeals) {
				if (filteredMeals.size() >= maxLimit) break; // Early termination
				
				String description = meal.getDescription() != null ? meal.getDescription().toLowerCase() : "";
				String title = meal.getTitle() != null ? meal.getTitle().toLowerCase() : "";
				
				if (containsIngredient(description, title, searchIngredient)) {
					filteredMeals.add(meal);
				}
			}

			if (filteredMeals.isEmpty()) {
				long filterDuration = System.currentTimeMillis() - filterStartTime;
				long totalDuration = System.currentTimeMillis() - startTime;
				log.info("✅ getMenuMealsByIngredient completed in {}ms (DB: {}ms, Filter: {}ms) - No meals found for ingredient: {}", 
						totalDuration, dbDuration, filterDuration, ingredient);
				return new MenuMealsAiResponse("Hiện chưa có món chứa " + ingredient + ".", Collections.emptyList());
			}

			// 3. Fast mapping with limit
			int requested = (limit != null ? limit : 4);
			int finalLimit = Math.min(Math.min(requested, 4), filteredMeals.size());
			List<MenuMealLiteResponse> limited = new java.util.ArrayList<>();
			
			for (int i = 0; i < finalLimit; i++) {
				MenuMealResponse m = filteredMeals.get(i);
				limited.add(new MenuMealLiteResponse(
					m.getId(),
					m.getTitle(),
					m.getSlug(),
					m.getImage(),
					m.getDescription(),
					m.getCarbs(),
					m.getCalories(),
					m.getProtein(),
					m.getFat(),
					m.getPrice(),
					null
				));
			}

			// 4. Generate content
			String content = String.format("Dưới đây là các món có %s:", ingredient);
			long filterDuration = System.currentTimeMillis() - filterStartTime;
			
			long totalDuration = System.currentTimeMillis() - startTime;
			int menuSize = limited.size();
			log.info("✅ getMenuMealsByIngredient completed in {}ms (DB: {}ms, Filter: {}ms) - Returned {} meals", 
					totalDuration, dbDuration, filterDuration, menuSize);

			return new MenuMealsAiResponse(content, limited);
		} catch (Exception ex) {
			long totalDuration = System.currentTimeMillis() - startTime;
			log.error("❌ getMenuMealsByIngredient failed after {}ms: {}", totalDuration, ex.getMessage());
			// Return empty response instead of throwing exception to prevent timeout
			return new MenuMealsAiResponse("Xin lỗi, hiện tại không thể tìm kiếm món ăn. Vui lòng thử lại sau.", Collections.emptyList());
		}
	}

	// Helper method to check if meal contains ingredient with synonyms - OPTIMIZED
	private boolean containsIngredient(String description, String title, String searchIngredient) {
		// Fast path: check direct match first
		if (description.contains(searchIngredient) || title.contains(searchIngredient)) {
			return true;
		}
		
		// Từ đồng nghĩa cho tôm - optimized
		if (isShrimpRelated(searchIngredient)) {
			return containsAny(description, title, "tôm", "shrimp", "prawns", "prawn");
		}
		
		// Từ đồng nghĩa cho cá - optimized
		if (isFishRelated(searchIngredient)) {
			return containsAny(description, title, "cá", "fish", "salmon", "tuna");
		}
		
		// Từ đồng nghĩa cho bò - optimized
		if (isBeefRelated(searchIngredient)) {
			return containsAny(description, title, "bò", "beef", "wagyu", "steak");
		}
		
		// Từ đồng nghĩa cho gà - optimized
		if (isChickenRelated(searchIngredient)) {
			return containsAny(description, title, "gà", "chicken", "poultry", "hen");
		}
		
		// Từ đồng nghĩa cho hải sản - optimized
		if (isSeafoodRelated(searchIngredient)) {
			return containsAny(description, title, "hải sản", "seafood", "tôm", "cá", "cua", "mực");
		}
		
		return false;
	}
	
	// Helper method to check if description or title contains any of the given keywords
	private boolean containsAny(String description, String title, String... keywords) {
		for (String keyword : keywords) {
			if (description.contains(keyword) || title.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	private boolean isShrimpRelated(String ingredient) {
		return ingredient.equals("tôm") || ingredient.equals("shrimp") || 
			   ingredient.equals("prawns") || ingredient.equals("prawn") ||
			   ingredient.equals("lobster") || ingredient.equals("crayfish");
	}

	private boolean isFishRelated(String ingredient) {
		return ingredient.equals("cá") || ingredient.equals("fish") || 
			   ingredient.equals("salmon") || ingredient.equals("tuna") ||
			   ingredient.equals("cod") || ingredient.equals("sea bass");
	}

	private boolean isBeefRelated(String ingredient) {
		return ingredient.equals("bò") || ingredient.equals("beef") || 
			   ingredient.equals("wagyu") || ingredient.equals("steak") ||
			   ingredient.equals("prime rib");
	}

	private boolean isChickenRelated(String ingredient) {
		return ingredient.equals("gà") || ingredient.equals("chicken") || 
			   ingredient.equals("poultry") || ingredient.equals("hen");
	}

	private boolean isSeafoodRelated(String ingredient) {
		return ingredient.equals("hải sản") || ingredient.equals("seafood");
	}

	@Tool(name = "getMenuMealsByPrice", description = """
			***CRITICAL***: CHỈ gọi tool này khi user HỎI CỤ THỂ về giá món ăn. KHÔNG gọi cho greetings, chitchat, hoặc câu hỏi chung.

			# PURPOSE
			Lấy danh sách menu meals theo khoảng giá cụ thể để tư vấn món ăn phù hợp với ngân sách.

			# WHEN TO CALL - CHỈ gọi khi user hỏi CỤ THỂ về giá:
			✅ Vietnamese: "Có món nào dưới 100k không?", "Món nào từ 50k đến 150k?", "Món rẻ nhất là gì?", "Món đắt nhất là gì?", "Có món nào giá trung bình không?", "Món nào dưới 1 triệu?"
			✅ English: "Do you have meals under 100k?", "What meals are between 50k and 150k?", "What's the cheapest meal?", "What's the most expensive meal?", "Any meals with average price?", "Any meals under 1 million?"

			# WHEN NOT TO CALL - TUYỆT ĐỐI KHÔNG gọi khi:
			❌ Vietnamese: "Hello", "Hi", "Chào", "Bạn khỏe không?", "Cảm ơn", "OK", "Test", "Bạn có thể giúp không?"
			❌ English: "Hello", "Hi", "How are you?", "Thanks", "Thank you", "OK", "Test", "Can you help?", "What can you do?"

			# PARAMETERS
			- minPrice (integer, optional): Giá tối thiểu (VND). Mặc định null (không giới hạn tối thiểu).
			- maxPrice (integer, optional): Giá tối đa (VND). Mặc định null (không giới hạn tối đa).
			- sortBy (string, optional): Sắp xếp theo "price_asc" (rẻ nhất trước) hoặc "price_desc" (đắt nhất trước). Mặc định null (không sắp xếp).
			- limit (integer, optional): Số món tối đa cần trả. Mặc định 4. Luôn giới hạn tối đa 4.

			# EXAMPLES:
			User: "Có món nào dưới 100k không?" → minPrice=null, maxPrice=100000
			User: "Món nào từ 50k đến 150k?" → minPrice=50000, maxPrice=150000
			User: "Món rẻ nhất là gì?" → sortBy="price_asc", limit=1
			User: "Món đắt nhất là gì?" → sortBy="price_desc", limit=1
			User: "5 món rẻ nhất" → sortBy="price_asc", limit=5

			# ERROR HANDLING
			- Nếu không có món phù hợp: trả {"content": "Hiện chưa có món phù hợp với khoảng giá này.", "menu": []}
			- Nếu xảy ra lỗi hệ thống: ném exception để lớp ngoài xin lỗi người dùng.
			""")
	public MenuMealsAiResponse getMenuMealsByPrice(Integer minPrice, Integer maxPrice, String sortBy, Integer limit) {
		long startTime = System.currentTimeMillis();
		log.info("🚀 Starting getMenuMealsByPrice with minPrice: {}, maxPrice: {}, sortBy: {}, limit: {}", minPrice, maxPrice, sortBy, limit);
		
		try {
			// 1. Database query
			long dbStartTime = System.currentTimeMillis();
			List<MenuMealResponse> allMeals = menuMealService.getAllMenuMeals();
			long dbDuration = System.currentTimeMillis() - dbStartTime;
			log.info("🗄️ Database query completed in {}ms", dbDuration);
			
			// 2. Filtering and processing
			long filterStartTime = System.currentTimeMillis();
			List<MenuMealResponse> filteredMeals = allMeals.stream()
				.filter(meal -> {
					if (meal.getPrice() == null) return false;
					
					boolean matchesMin = (minPrice == null) || (meal.getPrice() >= minPrice);
					boolean matchesMax = (maxPrice == null) || (meal.getPrice() <= maxPrice);
					
					return matchesMin && matchesMax;
				})
				.collect(Collectors.toList());

			if (filteredMeals.isEmpty()) {
				long filterDuration = System.currentTimeMillis() - filterStartTime;
				long totalDuration = System.currentTimeMillis() - startTime;
				log.info("✅ getMenuMealsByPrice completed in {}ms (DB: {}ms, Filter: {}ms) - No meals found for price range", 
						totalDuration, dbDuration, filterDuration);
				return new MenuMealsAiResponse("Hiện chưa có món phù hợp với khoảng giá này.", Collections.emptyList());
			}

			// 3. Sorting
			if (sortBy != null) {
				switch (sortBy.toLowerCase()) {
					case "price_asc":
						filteredMeals.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
						break;
					case "price_desc":
						filteredMeals.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
						break;
				}
			}

			// 4. Limiting
			int requested = (limit != null ? limit : 4);
			int maxItems = Math.min(Math.min(requested, 4), filteredMeals.size());
			List<MenuMealLiteResponse> limited = filteredMeals.stream().limit(maxItems)
				.map(m -> new MenuMealLiteResponse(
					m.getId(),
					m.getTitle(),
					m.getSlug(),
					m.getImage(),
					m.getDescription(), // DESCRIPTION CHỨA NGUYÊN LIỆU
					m.getCarbs(),
					m.getCalories(),
					m.getProtein(),
					m.getFat(),
					m.getPrice(),
					null // menuIngredients đã bị hủy bỏ
				))
				.toList();

			// 5. Generate content based on search criteria
			String content = generatePriceContent(minPrice, maxPrice, sortBy, limited);
			long filterDuration = System.currentTimeMillis() - filterStartTime;
			
			long totalDuration = System.currentTimeMillis() - startTime;
			int contentChars = content != null ? content.length() : 0;
			int menuSize = limited.size();
			log.info("✅ getMenuMealsByPrice completed in {}ms (DB: {}ms, Filter: {}ms) - Returned {} meals, contentChars={}", 
					totalDuration, dbDuration, filterDuration, menuSize, contentChars);

			return new MenuMealsAiResponse(content, limited);
		} catch (Exception ex) {
			long totalDuration = System.currentTimeMillis() - startTime;
			log.error("❌ getMenuMealsByPrice failed after {}ms: {}", totalDuration, ex.getMessage());
			throw ex;
		}
	}

	// Helper method to generate content based on price search criteria
	private String generatePriceContent(Integer minPrice, Integer maxPrice, String sortBy, List<MenuMealLiteResponse> meals) {
		if (meals.isEmpty()) {
			return "Hiện chưa có món phù hợp với khoảng giá này.";
		}

		if (sortBy != null) {
			switch (sortBy.toLowerCase()) {
				case "price_asc":
					return String.format("Dưới đây là %d món rẻ nhất trong menu:", meals.size());
				case "price_desc":
					return String.format("Dưới đây là %d món đắt nhất trong menu:", meals.size());
			}
		}

		if (minPrice != null && maxPrice != null) {
			return String.format("Dưới đây là các món có giá từ %,d VND đến %,d VND:", minPrice, maxPrice);
		} else if (minPrice != null) {
			return String.format("Dưới đây là các món có giá từ %,d VND trở lên:", minPrice);
		} else if (maxPrice != null) {
			return String.format("Dưới đây là các món có giá dưới %,d VND:", maxPrice);
		}

		return "Dưới đây là các món trong menu:";
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