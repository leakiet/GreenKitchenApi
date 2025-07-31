package com.greenkitchen.portal.dtos;
import java.util.List;

import com.greenkitchen.portal.dtos.MenuMealResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MenuMealsAiResponse {
    private String type; // "MENU_LIST" hoặc "EMPTY_MENU"
    private String message; // Intro hoặc báo menu trống
    private List<MenuMealResponse> menu;
}
