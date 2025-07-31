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

    @Tool(description = "Lấy danh sách menu meals hoặc món ăn trong menu. Trả về danh sách các món ăn có trong menu hoặc message nếu trống.")
    public MenuMealsAiResponse getMenuMeals() {
        List<MenuMealResponse> meals = menuMealService.getAllMenuMeals();
        if (meals == null || meals.isEmpty()) {
            return new MenuMealsAiResponse(
                "EMPTY_MENU",
                "Hiện tại menu đang trống. Em sẽ cập nhật món mới sớm nhất. Anh/chị cần tư vấn dinh dưỡng, món ăn lành mạnh thì em luôn sẵn sàng hỗ trợ!",
                Collections.emptyList()
            );
        }
        return new MenuMealsAiResponse(
            "MENU_LIST",
            "Dưới đây là các món trong menu của Green Kitchen, anh/chị tham khảo nhé:",
            meals
        );
    }



}
