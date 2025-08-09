package com.greenkitchen.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.IngredientActHisRequest;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.IngredientActHis;
import com.greenkitchen.portal.entities.Ingredients;
import com.greenkitchen.portal.enums.IngredientActionType;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.services.IngredientActHisService;
import com.greenkitchen.portal.services.IngredientService;


@RestController
@RequestMapping("/apis/v1/ingredient-act-his")
public class IngredientActHisController {
  @Autowired
    private IngredientActHisService ingredientActHisService;

  @Autowired
    private CustomerService customerService;

  @Autowired
    private IngredientService ingredientService;
    
  @Autowired
  public IngredientActHisController(IngredientActHisService ingredientActHisService) {
    this.ingredientActHisService = ingredientActHisService;
  } 

  @GetMapping
  public ResponseEntity<List<IngredientActHis>> getAllIngredientActions() {
    List<IngredientActHis> actions = ingredientActHisService.findAll();
    return ResponseEntity.ok(actions);
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<List<IngredientActHis>> getActionsByCustomerId(@PathVariable("customerId") Long customerId) {
    List<IngredientActHis> actions = ingredientActHisService.findByCustomerId(customerId);
    return ResponseEntity.ok(actions);
  }

  @PostMapping("/customers")
  public ResponseEntity<IngredientActHis> addIngredientAction(@RequestBody IngredientActHisRequest req) {
    Customer customer = customerService.findById(req.getCustomerId());
    if (customer == null) {
        throw new RuntimeException("Customer not found");
    }
    Ingredients ingredient = ingredientService.findById(req.getIngredientId());
    if (ingredient == null) {
        throw new RuntimeException("Ingredient not found");
    }
    IngredientActHis entity = new IngredientActHis();
    entity.setCustomer(customer);
    entity.setIngredient(ingredient);
    entity.setActionType(IngredientActionType.valueOf(req.getActionType()));
    IngredientActHis saved = ingredientActHisService.save(entity);
    return ResponseEntity.ok(saved);
  }
}
