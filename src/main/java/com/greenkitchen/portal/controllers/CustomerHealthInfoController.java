package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.CustomerHealthInfoRequest;
import com.greenkitchen.portal.dtos.CustomerHealthInfoResponse;
import com.greenkitchen.portal.services.CustomerHealthInfoService;

@CrossOrigin
@RestController
@RequestMapping("/apis/v1/customer-health-info")
public class CustomerHealthInfoController {

  @Autowired
  private CustomerHealthInfoService customerHealthInfoService;

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<CustomerHealthInfoResponse> getByCustomerId(@PathVariable("customerId") Long customerId) {
    CustomerHealthInfoResponse healthInfo = customerHealthInfoService.getByCustomerId(customerId);
    if (healthInfo == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(healthInfo);
  }

  @PostMapping
  public ResponseEntity<CustomerHealthInfoResponse> create(@RequestBody CustomerHealthInfoRequest request) {
    
    System.out.println("Creating Health Info: " + request.toString());
    CustomerHealthInfoResponse healthInfo = customerHealthInfoService.create(request);
    if (healthInfo == null) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(healthInfo);
  }

  @PutMapping("/customer/{customerId}")
  public ResponseEntity<CustomerHealthInfoResponse> update(@PathVariable("customerId") Long customerId,
      @RequestBody CustomerHealthInfoRequest request) {
    CustomerHealthInfoResponse healthInfo = customerHealthInfoService.update(customerId, request);
    return ResponseEntity.ok(healthInfo);
  }
}
