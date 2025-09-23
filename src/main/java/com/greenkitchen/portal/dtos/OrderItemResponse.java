package com.greenkitchen.portal.dtos;

import com.greenkitchen.portal.enums.OrderItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private OrderItemType itemType;

    // IDs for relationships
    private Long menuMealId;
    private Long customMealId;
    private Long weekMealId; //Thuc te day la customerWeekMealId

    private String title;
    private String description;
    private String image;

    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;

    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
