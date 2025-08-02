package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.CustomerReference;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerReferenceRepository extends JpaRepository<CustomerReference, Long> {
    List<CustomerReference> findByCustomerId(Long customerId);
    Optional<CustomerReference> findByCustomerIdAndId(Long customerId, Long id);
    void deleteByCustomerId(Long customerId);
}
