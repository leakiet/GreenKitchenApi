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

        Long customerId = customerReference.getCustomer().getId();
        
        // Kiểm tra customer có tồn tại không
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));

        // Kiểm tra xem customer đã có CustomerReference chưa (vì là OneToOne relationship)
        List<CustomerReference> existingReferences = customerReferenceRepository.findByCustomerId(customerId);
        if (!existingReferences.isEmpty()) {
            throw new IllegalArgumentException("Customer with id " + customerId + " already has a CustomerReference. Use update instead of create.");
        }

        // Set customer reference
        customerReference.setCustomer(customer);
        
        // Clear ID to ensure new entity
        customerReference.setId(null);
        
        // First save the CustomerReference to get the ID
        CustomerReference savedReference = customerReferenceRepository.save(customerReference);
        
        // Now set customerReference for all child entities and clear IDs for new entities
        if (customerReference.getFavoriteProteins() != null && !customerReference.getFavoriteProteins().isEmpty()) {
            customerReference.getFavoriteProteins().forEach(protein -> {
                protein.setId(null); // Ensure new entity
                protein.setCustomerReference(savedReference); // Set the saved reference with ID
            });
            // Re-set the list to trigger cascade save
            savedReference.setFavoriteProteins(customerReference.getFavoriteProteins());
        }
        
        if (customerReference.getFavoriteCarbs() != null && !customerReference.getFavoriteCarbs().isEmpty()) {
            customerReference.getFavoriteCarbs().forEach(carb -> {
                carb.setId(null); // Ensure new entity
                carb.setCustomerReference(savedReference); // Set the saved reference with ID
            });
            savedReference.setFavoriteCarbs(customerReference.getFavoriteCarbs());
        }
        
        if (customerReference.getFavoriteVegetables() != null && !customerReference.getFavoriteVegetables().isEmpty()) {
            customerReference.getFavoriteVegetables().forEach(vegetable -> {
                vegetable.setId(null); // Ensure new entity
                vegetable.setCustomerReference(savedReference); // Set the saved reference with ID
            });
            savedReference.setFavoriteVegetables(customerReference.getFavoriteVegetables());
        }
        
        if (customerReference.getAllergies() != null && !customerReference.getAllergies().isEmpty()) {
            customerReference.getAllergies().forEach(allergy -> {
                allergy.setId(null); // Ensure new entity
                allergy.setCustomerReference(savedReference); // Set the saved reference with ID
            });
            savedReference.setAllergies(customerReference.getAllergies());
        }
        
        // Save again to persist the child entities
        return customerReferenceRepository.save(savedReference);
    }

    @Override
    public CustomerReference updateCustomerReference(CustomerReference customerReference) {
        // Validate input
        if (customerReference == null) {
            throw new IllegalArgumentException("CustomerReference cannot be null");
        }
        
        if (customerReference.getCustomer() == null || customerReference.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Customer information is required");
        }

        Long customerId = customerReference.getCustomer().getId();
        
        // Kiểm tra customer có tồn tại không
        customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));

        // Tìm CustomerReference hiện tại
        List<CustomerReference> existingReferences = customerReferenceRepository.findByCustomerId(customerId);
        if (existingReferences.isEmpty()) {
            throw new IllegalArgumentException("No CustomerReference found for customer id: " + customerId);
        }

        CustomerReference existingReference = existingReferences.get(0);
        
        // Update basic fields
        existingReference.setVegetarianType(customerReference.getVegetarianType());
        existingReference.setCanEatEggs(customerReference.getCanEatEggs());
        existingReference.setCanEatDairy(customerReference.getCanEatDairy());
        existingReference.setNote(customerReference.getNote());

        // Clear old relationships
        if (existingReference.getFavoriteProteins() != null) {
            existingReference.getFavoriteProteins().clear();
        }
        if (existingReference.getFavoriteCarbs() != null) {
            existingReference.getFavoriteCarbs().clear();
        }
        if (existingReference.getFavoriteVegetables() != null) {
            existingReference.getFavoriteVegetables().clear();
        }
        if (existingReference.getAllergies() != null) {
            existingReference.getAllergies().clear();
        }

        // Set new relationships
        if (customerReference.getFavoriteProteins() != null) {
            customerReference.getFavoriteProteins().forEach(protein -> {
                protein.setId(null);
                protein.setCustomerReference(existingReference);
            });
            existingReference.setFavoriteProteins(customerReference.getFavoriteProteins());
        }
        
        if (customerReference.getFavoriteCarbs() != null) {
            customerReference.getFavoriteCarbs().forEach(carb -> {
                carb.setId(null);
                carb.setCustomerReference(existingReference);
            });
            existingReference.setFavoriteCarbs(customerReference.getFavoriteCarbs());
        }
        
        if (customerReference.getFavoriteVegetables() != null) {
            customerReference.getFavoriteVegetables().forEach(vegetable -> {
                vegetable.setId(null);
                vegetable.setCustomerReference(existingReference);
            });
            existingReference.setFavoriteVegetables(customerReference.getFavoriteVegetables());
        }
        
        if (customerReference.getAllergies() != null) {
            customerReference.getAllergies().forEach(allergy -> {
                allergy.setId(null);
                allergy.setCustomerReference(existingReference);
            });
            existingReference.setAllergies(customerReference.getAllergies());
        }

        return customerReferenceRepository.save(existingReference);
    }

    @Override
    public CustomerReference createOrUpdateCustomerReference(CustomerReference customerReference) {
        if (customerReference == null || customerReference.getCustomer() == null || customerReference.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Customer information is required");
        }

        Long customerId = customerReference.getCustomer().getId();
        List<CustomerReference> existingReferences = customerReferenceRepository.findByCustomerId(customerId);
        
        if (existingReferences.isEmpty()) {
            return createCustomerReference(customerReference);
        } else {
            return updateCustomerReference(customerReference);
        }
    }
}
