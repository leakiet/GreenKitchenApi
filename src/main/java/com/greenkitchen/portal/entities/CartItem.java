package com.greenkitchen.portal.entities;

import jakarta.persistence.*;
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
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Column(name = "is_custom")
    private Boolean isCustom = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_meal_id")
    private MenuMeal menuMeal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_meal_id")
    private CustomMeal customMeal;

    private Integer quantity;
    private Double basePrice;
    private Double totalPrice;
    private String title;
    private String description;
    
    @Embedded
    private NutritionInfo nutrition;
}
