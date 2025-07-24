package com.greenkitchen.portal.entities;

import java.util.ArrayList;
import java.util.List;

import com.greenkitchen.portal.enums.MenuMealType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "menu_meals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuMeal extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    private String title;
    private String description;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    @Enumerated(EnumType.STRING)
    private MenuMealType type;
    private String image;
    private Double price;
    private String slug;
    @OneToMany(mappedBy = "menuMeal", fetch = FetchType.LAZY)
    private List<MenuMealReview> reviews = new ArrayList<>();

}
