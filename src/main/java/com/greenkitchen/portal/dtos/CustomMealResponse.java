package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomMealResponse {
    private Long id;
    private Long customerId;
    private String name;
    private Double protein;
    private Double calories;
    private Double carb;
    private Double fat;
    private List<CustomMealDetailResponse> details;
}
