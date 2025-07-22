package com.greenkitchen.portal.dtos;

import java.util.List;

import com.greenkitchen.portal.enums.ActivityLevel;
import com.greenkitchen.portal.enums.HealthGoal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerHealthInfoResponse {
    private Long id;
    private Long customerId;
    private Integer age;
    private Double weight;
    private Double height;
    private ActivityLevel activityLevel;
    private HealthGoal goal;
    private List<String> allergies;
}
