package com.greenkitchen.portal.services;

import com.greenkitchen.portal.entities.CustomerTDEE;
import java.util.List;

public interface CustomerTDEEService {
  CustomerTDEE save(CustomerTDEE customerTDEE);
  List<CustomerTDEE> findByCustomerId(Long customerId);
  void deleteById(Long id);
}
