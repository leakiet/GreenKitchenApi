package com.greenkitchen.portal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_allergies")
@JsonIgnoreProperties({"customerReference"})
public class CustomerAllergy extends AbstractEntity {
    
    @ManyToOne
    @JoinColumn(name = "customer_reference_id", nullable = false)
    @JsonBackReference
    private CustomerReference customerReference;
    
    @Column(name = "allergy_name", nullable = false)
    private String allergyName;
}
