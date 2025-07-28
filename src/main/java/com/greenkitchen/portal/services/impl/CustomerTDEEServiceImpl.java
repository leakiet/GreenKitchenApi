package com.greenkitchen.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.greenkitchen.portal.entities.CustomerTDEE;
import com.greenkitchen.portal.repositories.CustomerTDEERepository;
import com.greenkitchen.portal.services.CustomerTDEEService;
import java.util.List;

@Service
public class CustomerTDEEServiceImpl implements CustomerTDEEService {

  @Autowired
  private CustomerTDEERepository customerTDEERepository;

  @Override
  public CustomerTDEE save(CustomerTDEE customerTDEE) {
    return customerTDEERepository.save(customerTDEE);
  }

  @Override
  public List<CustomerTDEE> findByCustomerId(Long customerId) {
    return customerTDEERepository.findByCustomerIdOrderByCalculationDateDesc(customerId);
  }

  @Override
  public void deleteById(Long id) {
    customerTDEERepository.deleteById(id);
  }
}
