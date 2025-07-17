package com.greenkitchen.portal.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.Employee;

public class MyUserDetails implements UserDetails {

	private static final long serialVersionUID = 995564934432043084L;
	
	private static final String TYPE_CUSTOMER = "CUSTOMER";
	private static final String TYPE_EMPLOYEE = "EMPLOYEE";
	
	private Customer customer;
	private Employee employee;
	private String userType;

	public MyUserDetails(Customer customer) {
		this.customer = customer;
		this.userType = TYPE_CUSTOMER;
	}

	public MyUserDetails(Employee employee) {
		this.employee = employee;
		this.userType = TYPE_EMPLOYEE;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		if (userType.equals(TYPE_EMPLOYEE) && employee != null && employee.getRole() != null) {
			authorities.add(new SimpleGrantedAuthority(employee.getRole().name()));
		} else {
			authorities.add(new SimpleGrantedAuthority("USER"));
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		if (userType.equals(TYPE_EMPLOYEE) && employee != null) {
			return employee.getPassword();
		}
		return customer != null ? customer.getPassword() : null;
	}

	@Override
	public String getUsername() {
		if (userType.equals(TYPE_EMPLOYEE) && employee != null) {
			return employee.getEmail();
		}
		return customer != null ? customer.getEmail() : null;
	}

	public List<String> getRoles() {
		List<String> roles = new ArrayList<>();
		if (userType.equals(TYPE_EMPLOYEE) && employee != null && employee.getRole() != null) {
			roles.add(employee.getRole().name());
		} else {
			roles.add("USER");
		}
		return roles;
	}

	public Customer getCustomer() {
		return customer;
	}

	public Employee getEmployee() {
		return employee;
	}

	public String getUserType() {
		return userType;
	}
	
}
