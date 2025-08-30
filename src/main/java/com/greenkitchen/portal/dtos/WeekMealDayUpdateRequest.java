package com.greenkitchen.portal.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeekMealDayUpdateRequest {
    private Long meal1Id;
    private Long meal2Id;
    private Long meal3Id;
}