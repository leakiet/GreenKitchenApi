package com.greenkitchen.portal.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.Employee;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.services.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {
	private final CustomerService customerService;
	private final EmployeeService employeeService;

	public MyUserDetailService(CustomerService customerService, EmployeeService employeeService) {
		this.customerService = customerService;
		this.employeeService = employeeService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Customer customer = customerService.findByEmail(username);
		if (customer != null) {
			return new MyUserDetails(customer);
		}
		Employee employee = employeeService.findByEmail(username);
		if (employee != null) {
			return new MyUserDetails(employee);
		}
		throw new UsernameNotFoundException("User not found with email: " + username);
	}

	public UserDetails loadUserByPhoneNumber(String phoneNumber) throws UsernameNotFoundException {
		try {
			// Find or create customer by phone number
			Customer customer = customerService.findOrCreateCustomerByPhone(phoneNumber);
			return new MyUserDetails(customer);
		} catch (Exception e) {
			throw new UsernameNotFoundException("Could not create or find user with phone: " + phoneNumber + " - " + e.getMessage());
		}
	}
}
