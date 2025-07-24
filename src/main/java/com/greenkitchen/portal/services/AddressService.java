package com.greenkitchen.portal.services;

import com.greenkitchen.portal.entities.Address;

public interface AddressService {
  Address createNew(Address address);
  Address update(Address address);
  void deleteById(Long id);
  Address existsById(Long id);
}
