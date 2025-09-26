package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponse {
    private long id;
    private String fullName;
    private String email;
    private String avatar;
    private String role;
    private String token;
    private String refreshToken;
    private String tokenType;
}
