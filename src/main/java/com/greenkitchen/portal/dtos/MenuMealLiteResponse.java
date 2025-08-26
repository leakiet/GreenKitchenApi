package com.greenkitchen.portal.dtos;

import java.util.Set;

import com.greenkitchen.portal.enums.MenuIngredients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuMealLiteResponse {
	private Long id;
	private String title;
	private String slug;
	private String image;
	private Double carb; // carbs
	private Double calo; // calories
	private Double fat;
	private Double price;
	private Set<MenuIngredients> menuIngredient; // menuIngredients
}


