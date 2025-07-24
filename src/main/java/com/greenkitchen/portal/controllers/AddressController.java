package com.greenkitchen.portal.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.AddressRequest;
import com.greenkitchen.portal.dtos.AddressResponse;
import com.greenkitchen.portal.entities.Address;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.services.AddressService;
import com.greenkitchen.portal.services.CustomerService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/apis/v1/addresses")
public class AddressController {
  
  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private AddressService addressService;
  
  @Autowired
  private CustomerService customerService;

  @PostMapping("/create")
  public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressRequest request) {
    Customer customer = customerService.findById(request.getCustomerId());
    if (customer == null) {
      throw new RuntimeException("Customer not found with id: " + request.getCustomerId());
    }
    
    Address address = modelMapper.map(request, Address.class);
    address.setCustomer(customer);
    
    Address savedAddress = addressService.createNew(address);
    
    AddressResponse response = modelMapper.map(savedAddress, AddressResponse.class);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/update")
  public ResponseEntity<AddressResponse> updateAddress(@Valid @RequestBody AddressRequest request) {
    if (request.getId() == null) {
      throw new RuntimeException("Address ID is required for update");
    }
    
    Customer customer = customerService.findById(request.getCustomerId());
    if (customer == null) {
      throw new RuntimeException("Customer not found with id: " + request.getCustomerId());
    }
    
    Address address = modelMapper.map(request, Address.class);
    address.setCustomer(customer);
    
    Address updatedAddress = addressService.update(address);
    
    AddressResponse response = modelMapper.map(updatedAddress, AddressResponse.class);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<String> deleteAddress(@PathVariable("id") Long id) {
    if (id == null) {
      throw new RuntimeException("Address ID is required for deletion");
    }

    addressService.deleteById(id);
    return ResponseEntity.ok("Address deleted successfully");
  }

}