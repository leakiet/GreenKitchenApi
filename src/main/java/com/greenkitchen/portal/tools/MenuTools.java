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
	@Autowired
	MenuMealAIService menuMealAIService;

	@Tool(name = "getMenuMeals", description = """
			    # PURPOSE
			    Lấy danh sách *menu meals* (các món ăn trong menu) cho khách hàng của Green Kitchen.

			    # WHEN TO CALL
			    – **Chỉ** gọi hàm này nếu (và chỉ nếu) người dùng:
			      • hỏi trực tiếp về menu hôm nay / hôm nay có món gì,
			      • yêu cầu xem món, giá, calorie, khẩu phần,
			      • hoặc đề cập cụ thể “thịt bò”, “ức gà”, “salad … trong menu” v.v.
			    – Nếu người dùng KHÔNG hỏi gì liên quan menu → KHÔNG gọi hàm, chỉ trả lời tự nhiên bằng tiếng Việt.

			    # RESPONSE FORMAT (bắt buộc khi hàm được gọi)
			    Trả về *một* đối tượng JSON duy nhất gồm 2 field:
			    1. **"content"**  : mô tả ngắn bằng *tiếng Việt* (chuỗi, rỗng nếu không có).
			    2. **"menu"**     : mảng các món ăn; GIỮ Y NGUYÊN tên trường tiếng Anh như trong DB (id, title, price, image, calories, type …).

			    > TUYỆT ĐỐI không thêm, đổi, dịch key; không bọc JSON trong markdown;
			    > không thêm giải thích bên ngoài JSON khi trả về dưới dạng function call.

			    # VALID EXAMPLE
			    {
			      "content": "Dưới đây là các món phù hợp chế độ eat‑clean hôm nay ạ:",
			      "menu": [
					         {
			       "id": 3,
			             "title": "Salmon Sweet Potato Bowl",
			             "description": "Salmon with sweet potato and steamed broccoli",
			             "calories": 480.0,
			             "protein": 30.0,
			             "carbs": 38.0,
			             "fat": 20.0,
			             "image": "https://res.cloudinary.com/quyendev/image/upload/v1753600158/kgtvrrfwyn2itqldkqvo.png",
			             "price": 17.99,
			             "slug": "salmon-sweet-potato-bowl",
			             "type": "BALANCE",
			             "reviews": []
			       },
			        // … tối đa <limit> món
			      ]
			    }

			    # ERROR HANDLING
			    – Nếu DB trả về rỗng, vẫn trả JSON với menu = [] và content giải thích “hiện chưa có món phù hợp”.
			    – Nếu xảy ra lỗi hệ thống → trả lời tiếng Việt nói xin lỗi & hướng dẫn liên hệ CSKH; KHÔNG bịa dữ liệu.

			    # LANGUAGE
			    – Mọi văn bản người dùng thấy (content) luôn bằng tiếng Việt.
			    – Trường JSON luôn tiếng Anh.
			""")
	public MenuMealsAiResponse getMenuMeals() {
		List<MenuMealResponse> meals = menuMealService.getAllMenuMeals();
		// Kiểm tra nếu danh sách món ăn rỗng hoặc null
		if (meals == null || meals.isEmpty()) {
			return new MenuMealsAiResponse("EMPTY_MENU",
					"Hiện tại menu đang trống. Em sẽ cập nhật món mới sớm nhất. Anh/chị cần tư vấn dinh dưỡng, món ăn lành mạnh thì em luôn sẵn sàng hỗ trợ!",
					Collections.emptyList());
		}

		// Lấy tối đa 3 món đầu tiên để test nhanh
		int n = Math.min(3, meals.size());
		List<MenuMealResponse> firstThreeMeals = meals.subList(0, n);

		return new MenuMealsAiResponse("MENU_LIST", "Dưới đây là 3 món trong menu của Green Kitchen để test nhanh:",
				firstThreeMeals);
	}

}
