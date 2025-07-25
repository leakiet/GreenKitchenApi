package com.greenkitchen.portal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menu_meal_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuMealReview extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "menu_meal_id")
    @JsonIgnore
    private MenuMeal menuMeal;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Min(1)
    @Max(5)
    private Integer rating;
    private String comment;
}