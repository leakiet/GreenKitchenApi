package com.greenkitchen.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerReference;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.CustomerReferenceRepository;
import com.greenkitchen.portal.services.CustomerReferenceService;

import java.util.List;

@Service
@Transactional
public class CustomerReferenceServiceImpl implements CustomerReferenceService {

    @Autowired
    private CustomerReferenceRepository customerReferenceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public List<CustomerReference> getCustomerReferencesByCustomerId(Long customerId) {
        // Kiểm tra customer có tồn tại không
        customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));
        
        return customerReferenceRepository.findByCustomerId(customerId);
    }

    @Override
    public CustomerReference createCustomerReference(CustomerReference customerReference) {
        // Validate input
        if (customerReference == null) {
            throw new IllegalArgumentException("CustomerReference cannot be null");
        }
        
        if (customerReference.getCustomer() == null || customerReference.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Customer information is required");
        }

        // Kiểm tra customer có tồn tại không
        Customer customer = customerRepository.findById(customerReference.getCustomer().getId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerReference.getCustomer().getId()));

        // Set customer reference
        customerReference.setCustomer(customer);
        
        // Set customerReference for all child entities
        if (customerReference.getFavoriteProteins() != null) {
            customerReference.getFavoriteProteins().forEach(protein -> 
                protein.setCustomerReference(customerReference)
            );
        }
        
        if (customerReference.getFavoriteCarbs() != null) {
            customerReference.getFavoriteCarbs().forEach(carb -> 
                carb.setCustomerReference(customerReference)
            );
        }
        
        if (customerReference.getFavoriteVegetables() != null) {
            customerReference.getFavoriteVegetables().forEach(vegetable -> 
                vegetable.setCustomerReference(customerReference)
            );
        }
        
        if (customerReference.getAllergies() != null) {
            customerReference.getAllergies().forEach(allergy -> 
                allergy.setCustomerReference(customerReference)
            );
        }
        
        // Save customer reference
        return customerReferenceRepository.save(customerReference);
    }
}
