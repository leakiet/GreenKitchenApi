package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    Customer findByEmail(String email);
    Customer findByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE COALESCE(c.isDeleted, false) = false " +
        "AND (:q IS NULL OR c.firstName LIKE CONCAT('%', :q, '%') " +
        "OR c.lastName LIKE CONCAT('%', :q, '%') " +
        "OR c.email LIKE CONCAT('%', :q, '%') " +
        "OR c.phone LIKE CONCAT('%', :q, '%')) " +
        "ORDER BY c.createdAt DESC")
    Page<Customer> findFilteredPaged(@Param("q") String q, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND COALESCE(c.isDeleted, false) = false")
    List<Customer> findActiveCustomers();
}
