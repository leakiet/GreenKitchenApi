package com.greenkitchen.portal.entities;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipientName;

    private String recipientPhone;

    private String street;
    
    private String ward;
    
    private String district;
    
    private String city;
    
    private Boolean isDefault = false;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private Customer customer;

    // Method to get full address
    public String getFullAddress() {
        return String.format("%s, Phường %s, Quận %s, %s", street, ward, district, city);
    }
    
    // Method to get full address with customer name
    public String getFullAddressWithCustomer() {
        String customerName = customer != null ? customer.getFirstName() + " " + customer.getLastName() : "Unknown";
        return String.format("%s - %s", customerName, getFullAddress());
    }
}
