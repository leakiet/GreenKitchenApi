package com.greenkitchen.portal.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerMembership;

@Repository
public interface CustomerMembershipRepository extends JpaRepository<CustomerMembership, Long> {
    Optional<CustomerMembership> findByCustomer(Customer customer);
}
