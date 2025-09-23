package com.greenkitchen.portal.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.greenkitchen.portal.enums.OrderItemType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private OrderItemType itemType;

    // Liên kết với MenuMeal (nullable vì có thể là CustomMeal)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_meal_id")
    @JsonBackReference
    private MenuMeal menuMeal;

    // Liên kết với CustomMeal (nullable vì có thể là MenuMeal)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "custom_meal_id")
    @JsonBackReference
    private CustomMeal customMeal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_meal_id")
    @JsonBackReference
    private WeekMeal weekMeal;

    private String title;
    private String description;
    private String image;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Double unitPrice = 0.0;

    @Column(nullable = false)
    private Double totalPrice = 0.0;

    private String notes;
}
