package com.greenkitchen.portal.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerWeekMealDayUpdateRequest {
    private Long meal1Id;
    private Long meal2Id;
    private Long meal3Id;
}
