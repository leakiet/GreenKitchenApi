package com.greenkitchen.portal.dtos;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MenuMealsAiResponse {
    @JsonProperty("content") // đúng key yêu cầu
    private String content;

    @JsonProperty("menu")
    private List<MenuMealResponse> menu ;

}
