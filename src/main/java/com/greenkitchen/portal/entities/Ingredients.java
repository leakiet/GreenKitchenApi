package com.greenkitchen.portal.entities;

import com.greenkitchen.portal.enums.IngredientType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Ingredients extends AbstractEntity {
    private static final long serialVersionUID = 1L;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private IngredientType type;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String image;
}
