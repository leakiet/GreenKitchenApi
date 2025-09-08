package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerSummaryDto {
    private Long id;
    private String avatar;
    private String firstName;
    private String lastName;
    private String fullName;
    private String gender;
    private String email;
    private String phone;
}
