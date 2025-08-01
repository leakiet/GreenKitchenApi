package com.greenkitchen.portal.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.greenkitchen.portal.dtos.CustomerUpdateRequest;
import com.greenkitchen.portal.dtos.ChangePasswordRequest;
import com.greenkitchen.portal.dtos.EmailRequest;
import com.greenkitchen.portal.dtos.GoogleLinkRequest;
import com.greenkitchen.portal.dtos.UpdateAvatarResponse;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.services.CustomerService;

import org.springframework.web.bind.annotation.PutMapping;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/apis/v1/customers")
public class CustomerController {

  @Autowired
  private CustomerService customerService;

  @Autowired
  private ModelMapper modelMapper;

  @GetMapping("/email/{email}")
  public ResponseEntity<Customer> getCustomerByEmail(@PathVariable("email") String email) {
    try {
      Customer customer = customerService.findByEmail(email);

      if (customer == null) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok(customer);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/update")
  public ResponseEntity<String> updateBasicInfo(@Valid @RequestBody CustomerUpdateRequest request) {
    Customer customer = modelMapper.map(request, Customer.class);
    customerService.update(customer);
    return ResponseEntity.ok("Customer information updated successfully");
  }

  @PutMapping("/updateAvatar/{email}")
  public ResponseEntity<UpdateAvatarResponse> updateAvatar(
      @PathVariable("email") String email,
      @RequestParam("imageFile") MultipartFile file) {

    if (file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    UpdateAvatarResponse response = customerService.updateAvatar(email, file);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/updatePassword")
  public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    customerService.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());
    return ResponseEntity.ok("Password changed successfully");
  }

  @PutMapping("/unlinkGoogle")
  public ResponseEntity<String> unlinkGoogle(@RequestBody EmailRequest request) {
    customerService.unlinkGoogle(request.getEmail());
    return ResponseEntity.ok("Google account unlinked successfully");
  }

  @PutMapping("/linkGoogle")
  public ResponseEntity<String> linkGoogle(@Valid @RequestBody GoogleLinkRequest request) {
    customerService.linkGoogle(request.getEmail(), request.getIdToken());
    return ResponseEntity.ok("Google account linked successfully");
  }
}
