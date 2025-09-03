package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dùng làm response cho AI để gói `content` và danh sách `menu` các món ăn.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuMealsAiResponse {
    private String content;
    private List<MenuMealLiteResponse> menu;
}
