package com.greenkitchen.portal.dtos;

import org.checkerframework.checker.units.qual.A;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhoneLoginRequest {
    
    @NotBlank(message = "Firebase ID token is required")
    private String firebaseIdToken;
    
    @Pattern(regexp = "^0[0-9]{9}$", message = "Invalid phone number format")
    private String phoneNumber;

}
