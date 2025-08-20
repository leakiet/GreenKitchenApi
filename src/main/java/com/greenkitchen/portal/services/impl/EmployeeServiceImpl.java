package com.greenkitchen.portal.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.greenkitchen.portal.entities.Employee;
import com.greenkitchen.portal.repositories.EmployeeRepository;
import com.greenkitchen.portal.services.EmployeeService;


@Service
public class EmployeeServiceImpl implements EmployeeService {
  @Autowired
  private EmployeeRepository employeeRepository;

  private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public Employee findByEmail(String email) {
    return employeeRepository.findByEmail(email);
  }

  @Override
  public Employee checkLogin(String email, String password) {
    Employee employee = employeeRepository.findByEmail(email);
    if (employee == null || !encoder.matches(password, employee.getPassword())) {
      throw new IllegalArgumentException("Invalid email or password");
    }
    if (!employee.getIsActive()) {
      throw new IllegalArgumentException("Account is not active. Please verify your email.");
    }

    if (employee.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }

    return employee;
  }
}
