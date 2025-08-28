package com.greenkitchen.portal.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyIncomeResponse {
    private double currentIncome;
    private String periodLabel; // "Ngày", "Tuần", "Tháng",...
}
