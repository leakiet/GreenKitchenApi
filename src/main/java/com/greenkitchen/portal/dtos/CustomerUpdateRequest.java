package com.greenkitchen.portal.dtos;

import com.greenkitchen.portal.enums.Gender;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {
  private String email;
  
  private String firstName;

  private String lastName;

  private String phone;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private String birthDate;
}
