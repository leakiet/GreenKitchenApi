package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.CustomerAllergy;
import java.util.List;

@Repository
public interface CustomerAllergyRepository extends JpaRepository<CustomerAllergy, Long> {
    List<CustomerAllergy> findByCustomerReferenceId(Long customerReferenceId);
    void deleteByCustomerReferenceId(Long customerReferenceId);
}
