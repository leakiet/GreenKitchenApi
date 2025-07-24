package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    
    private Long id; // For update operations
    
    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "Recipient is required")
    private String recipientName;

    @NotBlank(message = "Phone is required")
    private String recipientPhone;
    
    @NotBlank(message = "Ward is required")
    private String ward;
    
    @NotBlank(message = "District is required")
    private String district;
    
    @NotBlank(message = "City is required")
    private String city;
    
    private Boolean isDefault = false;

    private Long customerId;
}
