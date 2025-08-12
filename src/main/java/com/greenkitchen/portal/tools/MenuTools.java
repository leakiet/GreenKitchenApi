package com.greenkitchen.portal.tools;

import java.util.Collections;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.dtos.MenuMealsAiResponse;
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

			# WHEN TO CALL
			Luôn gọi hàm này nếu CURRENT_USER_MESSAGE (hoặc lịch sử gần nhất) chứa ý định về:
			- menu/món/giá/calorie(kcal)/khẩu phần/nguyên liệu/loại món/"hôm nay", hoặc tên món/nguyên liệu cụ thể.
			Kể cả người dùng nói "không JSON" → vẫn phải gọi hàm và trả JSON; khi đó viết ghi chú ngắn trong `content` rằng hệ thống chỉ hỗ trợ JSON cho menu.

			# PARAMETERS
			- limit (integer, optional): số món tối đa cần trả. Mặc định 10. Nếu vượt số món hiện có → giới hạn theo số món hiện có.

			# RESPONSE FORMAT (BẮT BUỘC)
			Trả về DUY NHẤT một JSON object:
			{
			  "content": "Chuỗi tiếng Việt ngắn gọn (có thể rỗng)",
			  "menu": [
			    {
			      "id": number,
			      "title": string,
			      "description": string,
			      "calories": number | null,
			      "protein": number | null,
			      "carbs": number | null,
			      "fat": number | null,
			      "image": string,
			      "price": number,
			      "slug": string,
			      "type": string,
			      "reviews": []
			    }
			  ]
			}
			- `menu` lấy nguyên bản từ DB; không đổi key, không dịch giá trị.
			- Không bao bọc JSON bằng markdown; không thêm text ngoài JSON.

			# VALID EXAMPLE
			{
			  "content": "Dưới đây là một số món trong menu hôm nay:",
			  "menu": [
			    {
			      "id": 3,
			      "title": "Salmon Sweet Potato Bowl",
			      "description": "Salmon with sweet potato and steamed broccoli",
			      "calories": 480.0,
			      "protein": 30.0,
			      "carbs": 38.0,
			      "fat": 20.0,
			      "image": "https://...",
			      "price": 17.99,
			      "slug": "salmon-sweet-potato-bowl",
			      "type": "BALANCE",		     
			      "reviews": []
			    }
			  ]
			}
			***IMPORTANT***:
			- Trả nguyên bản dữ liệu từ DB cho tất cả field trong `menu` (title, description, type…)
			- Không dịch, không chỉnh sửa giá trị trong `menu`.
			- Không format Markdown/HTML.


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
}
