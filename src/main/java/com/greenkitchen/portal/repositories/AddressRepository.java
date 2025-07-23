package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.Address;
import com.greenkitchen.portal.entities.Customer;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    // Find all addresses for a customer
    List<Address> findByCustomer(Customer customer);
    
    // Find all addresses for a customer ordered by default first
    List<Address> findByCustomerOrderByIsDefaultDesc(Customer customer);
    
    // Find default address for a customer
    Address findByCustomerAndIsDefaultTrue(Customer customer);
    
    // Find addresses by customer ID
    List<Address> findByCustomerId(Long customerId);
    
    // Find default address by customer ID
    Address findByCustomerIdAndIsDefaultTrue(Long customerId);
    
    // Count addresses for a customer
    long countByCustomer(Customer customer);
    
    // Check if customer has any address
    boolean existsByCustomer(Customer customer);
    
    // Delete address using native SQL to avoid cascade issues
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM addresses WHERE id = :id", nativeQuery = true)
    void deleteByIdNative(@Param("id") Long id);
}
