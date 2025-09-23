package com.greenkitchen.portal.dtos;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerWeekMealResponse {
    private Long id;
    private Long customerId;
    private String type;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<CustomerWeekMealDayResponse> days;
}
