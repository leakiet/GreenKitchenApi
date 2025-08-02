package com.greenkitchen.portal.entities;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.greenkitchen.portal.enums.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customers")
public class Customer extends AbstractEntity {

	private String firstName;

	private String lastName;

	private String avatar;

	@JsonIgnore
	private String password;

	private LocalDateTime passwordUpdatedAt;

	@Email
	@NotBlank(message = "Email is required")
	@Column(unique = true)
	private String email;

	private Date birthDate;

	@Enumerated(EnumType.STRING)
	private Gender gender = Gender.UNDEFINED;

	@Column(unique = true)
	private String phone;

	private Boolean isActive = false;

	@JsonIgnore
	private String verifyToken;

	@JsonIgnore
	private LocalDateTime verifyTokenExpireAt;

	private String oauthProvider; // "google", null cho traditional users

	private String oauthProviderId; // ID từ Google

	private Boolean isOauthUser = false; // false cho traditional users

	@OneToMany(mappedBy = "customer", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	private List<Address> addresses;

	@OneToMany(mappedBy = "customer", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	private List<CustomerTDEE> customerTDEEs;

	@OneToMany(mappedBy = "customer", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<PointHistory> pointHistories;

	@OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference  
	private CustomerMembership membership;

	@OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	private CustomerReference customerReference;

	public String getFullName() {
		return lastName + " " + firstName;
	}

}
