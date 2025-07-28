package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.greenkitchen.portal.entities.CustomerTDEE;
import com.greenkitchen.portal.services.CustomerTDEEService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/apis/v1/customer-tdees")
public class CustomerTDEEController {

  @Autowired
  private CustomerTDEEService customerTDEEService;

  @PostMapping
  public ResponseEntity<CustomerTDEE> save(@Valid @RequestBody CustomerTDEE customerTDEE) {
    try {
      CustomerTDEE savedTDEE = customerTDEEService.save(customerTDEE);
      return ResponseEntity.status(201).body(savedTDEE);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<List<CustomerTDEE>> getTDEEsByCustomerId(@PathVariable("customerId") Long customerId) {
    try {
      List<CustomerTDEE> tdeeList = customerTDEEService.findByCustomerId(customerId);
      return ResponseEntity.ok(tdeeList);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteById(@PathVariable("id") Long id) {
    try {
      customerTDEEService.deleteById(id);
      return ResponseEntity.ok("TDEE record deleted successfully");
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Failed to delete TDEE record");
    }
  }
}
