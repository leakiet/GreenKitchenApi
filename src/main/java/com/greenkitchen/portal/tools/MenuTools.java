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
			***IMPORTANT***: Không dịch bất kỳ giá trị nào trong menu. Trả nguyên văn từ DB.

			    # PURPOSE
			    Lấy danh sách *menu meals* (các món ăn trong menu) cho khách hàng của Green Kitchen.

			    # WHEN TO CALL
			    – **Chỉ** gọi hàm này nếu (và chỉ nếu) người dùng:
			      • hỏi trực tiếp về menu hôm nay / hôm nay có món gì,
			      • yêu cầu xem món, giá, calorie, khẩu phần,
			      • hoặc đề cập cụ thể “thịt bò”, “ức gà”, “salad … trong menu” v.v.
			    – Nếu người dùng KHÔNG hỏi gì liên quan menu → KHÔNG gọi hàm, chỉ trả lời tự nhiên

			    # RESPONSE FORMAT (bắt buộc khi hàm được gọi)
			    Trả về *một* đối tượng JSON duy nhất gồm 2 field:
			    1. "content"  : mô tả ngắn bằng *tiếng Việt* (chuỗi, rỗng nếu không có).
			    2. "menu"     : mảng các món ăn; GIỮ Y NGUYÊN tên trường tiếng Anh như trong DB (id, title, price, image, calories, type …).
				
			    > TUYỆT ĐỐI không thêm, đổi, dịch key; không bọc JSON trong markdown;
			    > không thêm giải thích bên ngoài JSON khi trả về dưới dạng function call.

			    # VALID EXAMPLE
			    {
			      "content": "Dưới đây là các món phù hợp chế độ eat clean hôm nay ạ:",
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
			             "reviews": "tHIS FOOD SO HIGH"
			       },
			        
			      ]
			    }


			    # ERROR HANDLING
			    – Nếu DB trả về rỗng, vẫn trả JSON với menu = [] và content giải thích “hiện chưa có món phù hợp”.
			    – Nếu xảy ra lỗi hệ thống → trả lời tiếng Việt nói xin lỗi & hướng dẫn liên hệ CSKH; KHÔNG bịa dữ liệu.

			""")
	  public MenuMealsAiResponse getMenuMeals(Integer limit) {
	    try {
	      List<MenuMealResponse> meals = menuMealService.getAllMenuMeals();
	      List<MenuMealResponse> list = (meals == null) ? Collections.emptyList() : meals;

	      int n = Math.max(1, Math.min(limit != null ? limit : 10, list.size()));
	      list = list.stream().limit(n).toList();

	      String content = list.isEmpty()
	        ? "Hiện tại menu đang trống. Em sẽ cập nhật món mới sớm nhất. Anh/chị cần tư vấn dinh dưỡng lành mạnh thì em luôn sẵn sàng hỗ trợ!"
	        : "Dưới đây là một số món trong menu của Green Kitchen ạ:";

	      return new MenuMealsAiResponse(content, list);
	    } catch (Exception ex) {
	      // Theo prompt: lỗi hệ thống -> không trả JSON, để layer ngoài phát ngôn xin lỗi
	      throw ex;
	    }
	  }
	

}
