package com.greenkitchen.portal.tools;

import java.util.Collections;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.dtos.MenuMealsAiResponse;
import com.greenkitchen.portal.services.MenuMealService;

@Component
public class MenuTools {
    private final MenuMealService menuMealService;
    public MenuTools(MenuMealService menuMealService) {
        this.menuMealService = menuMealService;
    }

    @Tool(description = """
    		Lấy danh sách menu meals (các món ăn trong menu) cho khách hàng.
    		**Kết quả trả về phải là một object JSON gồm 2 trường:**
    		- "content": mô tả ngắn về menu (bằng tiếng Anh, nếu không có mô tả có thể để chuỗi rỗng "")
    		- "menu": danh sách các món ăn (array). **Mỗi món ăn là một object giữ nguyên các trường tiếng Anh giống như trả ra từ cơ sở dữ liệu (vd: id, title, price, image, v.v)**.

    		**Yêu cầu:**
    		- Luôn trả về đúng key "menu" (là array), và "content".
    		- Không bao giờ trả về trường tiếng Việt hoặc format khác.
    		- Không thêm trường phụ, không chú thích ngoài JSON.

    		**Ví dụ JSON đúng:**
    		{
    		  "content": "Today's menu for healthy eaters.",
    		  "menu": [
    		    {
    		      "id": 1,
    		      "title": "Balanced Protein Bowl",
    		      "price": 15.99,
    		      "image": "https://...",
    		      "calories": 450,
    		      ...
    		    },
    		    ...
    		  ]
    		}
    		""")

    public MenuMealsAiResponse getMenuMeals() {
        List<MenuMealResponse> meals = menuMealService.getAllMenuMeals();
        // Kiểm tra nếu danh sách món ăn rỗng hoặc null
        if (meals == null || meals.isEmpty()) {
            return new MenuMealsAiResponse(
                "EMPTY_MENU",
                "Hiện tại menu đang trống. Em sẽ cập nhật món mới sớm nhất. Anh/chị cần tư vấn dinh dưỡng, món ăn lành mạnh thì em luôn sẵn sàng hỗ trợ!",
                Collections.emptyList()
            );
        }

        // Lấy tối đa 3 món đầu tiên để test nhanh
        int n = Math.min(3, meals.size());
        List<MenuMealResponse> firstThreeMeals = meals.subList(0, n);

        return new MenuMealsAiResponse(
            "MENU_LIST",
            "Dưới đây là 3 món trong menu của Green Kitchen để test nhanh:",
            firstThreeMeals
        );
    }


    
//    @Tool(description = "Lấy danh sách món ăn trong menu theo ID của menu")
//    public MenuMealsAiResponse getMenuMeals(Long menuId) {
//		@SuppressWarnings("unchecked")
//		List<MenuMealResponse> meals = (List<MenuMealResponse>) menuMealService.getMenuMealById((long) 1);
//		if (meals == null || meals.isEmpty()) {
//			return new MenuMealsAiResponse(
//				"EMPTY_MENU",
//				"Hiện tại menu đang trống. Em sẽ cập nhật món mới sớm nhất. Anh/chị cần tư vấn dinh dưỡng, món ăn lành mạnh thì em luôn sẵn sàng hỗ trợ!",
//				Collections.emptyList()
//			);
//		}
//		return new MenuMealsAiResponse(
//			"MENU_LIST",
//			"Dưới đây là các món trong menu của Green Kitchen, anh/chị tham khảo nhé:",
//			meals
//		);
//	}

}
