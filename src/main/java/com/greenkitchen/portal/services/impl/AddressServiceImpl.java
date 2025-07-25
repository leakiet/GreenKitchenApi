package com.greenkitchen.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.Address;
import com.greenkitchen.portal.services.AddressService;
import com.greenkitchen.portal.repositories.AddressRepository;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public Address createNew(Address address) {
      if(address == null) {
        throw new IllegalArgumentException("Address cannot be null");
      }
      
      return addressRepository.save(address);
    }

    @Override
    public Address existsById(Long id) {
      if(id == null) {
        throw new IllegalArgumentException("Address ID cannot be null");
      }
      return addressRepository.findById(id).orElse(null);
    }

    @Override
    public Address update(Address address) {
      if(address == null || address.getId() == null) {
        throw new IllegalArgumentException("Address and Address ID cannot be null for update");
      }
      
      if (!addressRepository.existsById(address.getId())) {
        throw new IllegalArgumentException("Address not found with id: " + address.getId());
      }
      
      return addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
      if (!addressRepository.existsById(id)) {
        throw new IllegalArgumentException("Address not found with id: " + id);
      }

      addressRepository.deleteByIdNative(id);
    }
  
}
