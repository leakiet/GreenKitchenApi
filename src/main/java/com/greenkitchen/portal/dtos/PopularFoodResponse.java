package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PopularFoodResponse {
    private List<PopularFoodItem> items;
    private String fromDate;
    private String toDate;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PopularFoodItem {
        private String type;
        private double percentage; // Tỷ lệ %
    }
}