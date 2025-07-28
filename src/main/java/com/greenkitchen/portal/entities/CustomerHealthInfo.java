package com.greenkitchen.portal.entities;

import java.util.List;

import com.greenkitchen.portal.enums.ActivityLevel;
import com.greenkitchen.portal.enums.HealthGoal;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_health_info")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class CustomerHealthInfo extends AbstractEntity {
    private static final long serialVersionUID = 1L;
    
    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    private Integer age;
    private Double weight; // kg
    private Double height; // cm
    
    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;
    
    @Enumerated(EnumType.STRING)
    private HealthGoal goal;
    
    @ElementCollection
    @CollectionTable(name = "customer_allergies", joinColumns = @JoinColumn(name = "health_info_id"))
    @Column(name = "allergy")
    private List<String> allergies;

}
