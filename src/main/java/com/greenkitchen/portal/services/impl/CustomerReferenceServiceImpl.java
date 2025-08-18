package com.greenkitchen.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.dtos.CustomerReferenceRequest;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerReference;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.CustomerReferenceRepository;
import com.greenkitchen.portal.services.CustomerReferenceService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
public class CustomerReferenceServiceImpl implements CustomerReferenceService {

    @Autowired
    private CustomerReferenceRepository customerReferenceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CustomerReference> getCustomerReferencesByCustomerId(Long customerId) {
        // Kiểm tra customer có tồn tại không
        customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));

        return customerReferenceRepository.findByCustomerId(customerId)
                .map(List::of)
                .orElseGet(List::of);
    }

    @Override
    public CustomerReference createCustomerReference(CustomerReferenceRequest request) {
        // Validate input
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        Long customerId = request.getCustomerId();
        // Kiểm tra customer có tồn tại không
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));

        // Kiểm tra xem customer đã có CustomerReference chưa (vì là OneToOne
        // relationship)
        var existing = customerReferenceRepository.findByCustomerId(customerId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Customer with id " + customerId
                    + " already has a CustomerReference. Use update instead of create.");
        }

        CustomerReference customerReference = new CustomerReference();
        customerReference.setCustomer(customer);
        customerReference.setNote(request.getNote());
        customerReference.setCanEatDairy(request.getCanEatDairy());
        customerReference.setCanEatEggs(request.getCanEatEggs());
        customerReference.setVegetarianType(request.getVegetarianType());

        customerReference.setFavoriteProteins(request.getFavoriteProteins());
        customerReference.setFavoriteCarbs(request.getFavoriteCarbs());
        customerReference.setFavoriteVegetables(request.getFavoriteVegetables());
        customerReference.setAllergies(request.getAllergies());

        // Set back-reference on child entities before saving so CascadeType.ALL
        // persists them
        if (customerReference.getFavoriteProteins() != null && !customerReference.getFavoriteProteins().isEmpty()) {
            customerReference.getFavoriteProteins().forEach(protein -> protein.setCustomerReference(customerReference));
        }
        if (customerReference.getFavoriteCarbs() != null && !customerReference.getFavoriteCarbs().isEmpty()) {
            customerReference.getFavoriteCarbs().forEach(carb -> carb.setCustomerReference(customerReference));
        }
        if (customerReference.getFavoriteVegetables() != null && !customerReference.getFavoriteVegetables().isEmpty()) {
            customerReference.getFavoriteVegetables().forEach(veg -> veg.setCustomerReference(customerReference));
        }
        if (customerReference.getAllergies() != null && !customerReference.getAllergies().isEmpty()) {
            customerReference.getAllergies().forEach(allergy -> allergy.setCustomerReference(customerReference));
        }

        // Save once; children will be cascaded
        return customerReferenceRepository.save(customerReference);
    }

    @Override
    public CustomerReference updateCustomerReference(CustomerReferenceRequest request) {
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        Long customerId = request.getCustomerId();
        // Ensure customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));

        // Get existing reference
        CustomerReference ref = customerReferenceRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("CustomerReference not found for customer id: " + customerId));

        // Update scalar fields
        ref.setCustomer(customer);
        ref.setVegetarianType(request.getVegetarianType());
        ref.setCanEatEggs(Boolean.TRUE.equals(request.getCanEatEggs()));
        ref.setCanEatDairy(Boolean.TRUE.equals(request.getCanEatDairy()));
        ref.setNote(request.getNote());

        // Clear and add new items in-place (avoid replacing collection instance)
        ref.getFavoriteProteins().clear();
        if (request.getFavoriteProteins() != null) {
            request.getFavoriteProteins().forEach(p -> p.setCustomerReference(ref));
            ref.getFavoriteProteins().addAll(request.getFavoriteProteins());
        }

        ref.getFavoriteCarbs().clear();
        if (request.getFavoriteCarbs() != null) {
            request.getFavoriteCarbs().forEach(c -> c.setCustomerReference(ref));
            ref.getFavoriteCarbs().addAll(request.getFavoriteCarbs());
        }

        ref.getFavoriteVegetables().clear();
        if (request.getFavoriteVegetables() != null) {
            request.getFavoriteVegetables().forEach(v -> v.setCustomerReference(ref));
            ref.getFavoriteVegetables().addAll(request.getFavoriteVegetables());
        }

        ref.getAllergies().clear();
        if (request.getAllergies() != null) {
            request.getAllergies().forEach(a -> a.setCustomerReference(ref));
            ref.getAllergies().addAll(request.getAllergies());
        }

        return customerReferenceRepository.save(ref);
    }
}
