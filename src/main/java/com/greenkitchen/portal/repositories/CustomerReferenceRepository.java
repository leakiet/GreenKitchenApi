package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.CustomerReference;
import java.util.Optional;

@Repository
public interface CustomerReferenceRepository extends JpaRepository<CustomerReference, Long> {
    Optional<CustomerReference> findByCustomerId(Long customerId);
    void deleteByCustomerId(Long customerId);
}
