package com.greenkitchen.portal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.greenkitchen.portal.enums.VegetarianType;
import jakarta.persistence.EnumType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_references")
public class CustomerReference extends AbstractEntity {

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "vegetarian_type")
    private VegetarianType vegetarianType;

    @Column(name = "can_eat_eggs")
    private Boolean canEatEggs = false;

    @Column(name = "can_eat_dairy")
    private Boolean canEatDairy = false;

    @Column(name = "note")
    private String note;

    // One-to-Many relationships with new entities
    @OneToMany(mappedBy = "customerReference", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<FavoriteProtein> favoriteProteins = new ArrayList<>();

    @OneToMany(mappedBy = "customerReference", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<FavoriteCarb> favoriteCarbs = new ArrayList<>();

    @OneToMany(mappedBy = "customerReference", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<FavoriteVegetable> favoriteVegetables = new ArrayList<>();

    @OneToMany(mappedBy = "customerReference", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<CustomerAllergy> allergies = new ArrayList<>();
}
