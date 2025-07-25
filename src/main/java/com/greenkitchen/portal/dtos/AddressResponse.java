package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    
    private Long id;
    private String recipientName;
    private String recipientPhone;
    private String street;
    private String ward;
    private String district;
    private String city;
    private Boolean isDefault;
    private Long customerId;
    private String fullAddress;
}
