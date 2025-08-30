package com.greenkitchen.portal.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.greenkitchen.portal.enums.OrderItemType;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonBackReference
    private Cart cart;

    @Column(name = "is_custom")
    private Boolean isCustom = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_meal_id")
    private MenuMeal menuMeal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_meal_id")
    private CustomMeal customMeal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_meal_id")
    private WeekMeal weekMeal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemType itemType;

    private String title;
    private String description;
    private String image;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Double totalPrice;

    @Embedded
    private NutritionInfo nutrition;
}
