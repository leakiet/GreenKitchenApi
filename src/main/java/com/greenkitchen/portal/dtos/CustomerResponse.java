package com.greenkitchen.portal.dtos;

import com.greenkitchen.portal.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String avatar;
    private String email;
    private Date birthDate;
    private Gender gender;
    private String phone;
    private Boolean isActive;
    private Boolean isPhoneLogin;
    private Boolean isEmailLogin;
    private String oauthProvider;
    private Boolean isOauthUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
