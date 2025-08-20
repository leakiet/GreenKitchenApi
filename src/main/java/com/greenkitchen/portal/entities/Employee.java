package com.greenkitchen.portal.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import com.greenkitchen.portal.enums.Role;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "employees")
public class Employee extends AbstractEntity {
  private String username;

  private String firstName;

  private String lastName;

  @JsonIgnore
  private String password;

  @Email
  private String email;

  private String phone;

  private Boolean isActive = true;

  @Enumerated(EnumType.STRING)
  private Role role;

  public String getFullName() {
		if (firstName == null && lastName == null) {
			return "Name Not Provided";
		}
		return lastName + " " + firstName;
	}

}
