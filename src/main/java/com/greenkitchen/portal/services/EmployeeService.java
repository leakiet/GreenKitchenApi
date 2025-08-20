package com.greenkitchen.portal.services;

import com.greenkitchen.portal.entities.Employee;

public interface EmployeeService {
  Employee findByEmail(String email);
  Employee checkLogin(String email, String password);
}
