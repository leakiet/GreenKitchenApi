package com.greenkitchen.portal.tools;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.dtos.MenuMealsAiResponse;
import com.greenkitchen.portal.enums.MenuMealType;
import com.greenkitchen.portal.services.MenuMealAIService;
import com.greenkitchen.portal.services.MenuMealService;

@Component
public class MenuTools {
	@Autowired
	MenuMealService menuMealService;
	
	@Tool(name = "getMenuMeals", description = """
			***IMPORTANT***: Buộc trả về JSON hợp lệ để FE render UI. KHÔNG markdown/HTML/text ngoài JSON. KHÔNG dịch/đổi key/ghi bịa dữ liệu trong `menu` (giữ nguyên key tiếng Anh như DB).

			# PURPOSE
			Lấy danh sách menu meals (các món trong menu) của Green Kitchen để phản hồi ở MENU_JSON_MODE theo Output Contract hệ thống.

			# WHEN TO CALL - CHỈ gọi khi user hỏi CỤ THỂ về:
			✅ "Menu có gì?", "Món nào ngon?", "Có món gì?"
			✅ "Giá bao nhiêu?", "Calorie bao nhiêu?", "Khẩu phần thế nào?"
			✅ "Nguyên liệu gì?", "Loại món gì?", "Món hôm nay có gì?"
			✅ Tên món cụ thể: "Salmon", "Beef", "Chicken"
			

			# EXAMPLES:
			User: "Hello" → NO tool call, response: "Chào anh/chị! Em có thể giúp gì ạ?"
			User: "Menu có gì?" → YES tool call, return menu JSON
			User: "Chào" → NO tool call, response: "Chào anh/chị! Em có thể giúp gì ạ?"
			User: "Món nào ngon?" → YES tool call, return menu JSON

			# PARAMETERS
			- limit (integer, optional): số món tối đa cần trả. Mặc định 10. Nếu vượt số món hiện có → giới hạn theo số món hiện có.

			
			# ERROR HANDLING
			- Nếu DB rỗng: trả {"content": "Hiện chưa có món phù hợp.", "menu": []}.
			- Nếu xảy ra lỗi hệ thống: ném exception để lớp ngoài xin lỗi người dùng; KHÔNG bịa dữ liệu.
			""")
	public MenuMealsAiResponse getMenuMeals(Integer limit) {
	    try {
	        List<MenuMealResponse> allMeals = menuMealService.getAllMenuMeals();
	        List<MenuMealResponse> meals = (allMeals == null) ? Collections.emptyList() : allMeals;

	        if (meals.isEmpty()) {
	            return new MenuMealsAiResponse("Hiện chưa có món phù hợp.", Collections.emptyList());
	        }

	        int maxItems = Math.min(limit != null ? limit : 10, meals.size());
	        List<MenuMealResponse> limited = meals.stream().limit(maxItems).toList();

	        String content = "Dưới đây là một số món trong menu của Green Kitchen ạ:";

	        return new MenuMealsAiResponse(content, limited);
	    } catch (Exception ex) {
	        throw ex; // để lớp xử lý ngoài bắt và phản hồi lỗi người dùng
	    }
	}

	@Tool(name = "getMenuMealsByType", description = """
			***IMPORTANT***: Buộc trả về JSON hợp lệ để FE render UI. KHÔNG markdown/HTML/text ngoài JSON.

			# PURPOSE
			Lấy danh sách menu meals theo type cụ thể (LOW, HIGH, BALANCE, VEGETARIAN) để gợi ý món ăn phù hợp với mục tiêu dinh dưỡng.

			# WHEN TO CALL - Gọi khi user cần món ăn theo type cụ thể:
			✅ "Tôi muốn giảm cân, gợi ý món gì?" → type: LOW
			✅ "Tôi muốn tăng cân, món nào phù hợp?" → type: HIGH  
			✅ "Món ăn cân bằng dinh dưỡng?" → type: BALANCE
			✅ "Món chay có gì?" → type: VEGETARIAN
			✅ "Người béo nên ăn gì?" → type: LOW
			✅ "Người gầy nên ăn gì?" → type: HIGH

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
	    try {
	        // Validate type parameter
	        MenuMealType mealType;
	        try {
	            mealType = MenuMealType.valueOf(type.toUpperCase());
	        } catch (IllegalArgumentException e) {
	            return new MenuMealsAiResponse("Loại món ăn không hợp lệ. Các loại có sẵn: LOW, HIGH, BALANCE, VEGETARIAN", Collections.emptyList());
	        }

	        List<MenuMealResponse> allMeals = menuMealService.getAllMenuMeals();
	        if (allMeals == null) {
	            return new MenuMealsAiResponse("Hiện chưa có món phù hợp.", Collections.emptyList());
	        }

	        // Filter by type
	        List<MenuMealResponse> filteredMeals = allMeals.stream()
	            .filter(meal -> meal.getType() == mealType)
	            .collect(Collectors.toList());

	        if (filteredMeals.isEmpty()) {
	            return new MenuMealsAiResponse("Hiện chưa có món phù hợp với loại " + type + ".", Collections.emptyList());
	        }

	        // Apply limit
	        int maxItems = Math.min(limit != null ? limit : 10, filteredMeals.size());
	        List<MenuMealResponse> limited = filteredMeals.stream().limit(maxItems).toList();

	        String content = String.format("Dưới đây là các món %s phù hợp với mục tiêu của bạn:", type);

	        return new MenuMealsAiResponse(content, limited);
	    } catch (Exception ex) {
	        throw ex;
	    }
	}

	@Tool(name = "getMenuMealsForBodyType", description = """
			***IMPORTANT***: Buộc trả về JSON hợp lệ để FE render UI. KHÔNG markdown/HTML/text ngoài JSON.

			# PURPOSE
			Gợi ý món ăn phù hợp với thể trạng và mục tiêu dinh dưỡng của người dùng.

			# WHEN TO CALL - Gọi khi user cần tư vấn dinh dưỡng theo thể trạng:
			✅ "Tôi béo, muốn giảm cân" → bodyType: "OVERWEIGHT", goal: "LOSE_WEIGHT"
			✅ "Tôi gầy, muốn tăng cân" → bodyType: "UNDERWEIGHT", goal: "GAIN_WEIGHT"
			✅ "Tôi muốn duy trì cân nặng" → bodyType: "NORMAL", goal: "MAINTAIN"
			✅ "Tôi muốn tăng cơ" → bodyType: "NORMAL", goal: "BUILD_MUSCLE"

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
	    try {
	        // Validate parameters
	        if (!isValidBodyType(bodyType) || !isValidGoal(goal)) {
	            return new MenuMealsAiResponse("Tham số không hợp lệ. BodyType: OVERWEIGHT/UNDERWEIGHT/NORMAL, Goal: LOSE_WEIGHT/GAIN_WEIGHT/MAINTAIN/BUILD_MUSCLE", Collections.emptyList());
	        }

	        // Determine meal type based on body type and goal
	        MenuMealType targetType = determineMealType(bodyType, goal);
	        
	        // Get meals by type
	        return getMenuMealsByType(targetType.name(), limit);
	        
	    } catch (Exception ex) {
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
}
