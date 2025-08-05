package com.greenkitchen.portal.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.Cart;
import com.greenkitchen.portal.enums.CartStatus;

public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci WHERE c.customerId = :customerId AND c.status = :status AND (ci.isDeleted = false OR ci IS NULL)")
    Optional<Cart> findByCustomerIdAndStatusWithActiveItems(@Param("customerId") Long customerId, @Param("status") CartStatus status);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci WHERE c.customerId = :customerId AND (ci.isDeleted = false OR ci IS NULL)")
    Optional<Cart> findByCustomerIdWithActiveItems(@Param("customerId") Long customerId);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci WHERE c.id = :id AND (ci.isDeleted = false OR ci IS NULL)")
    Optional<Cart> findByIdWithActiveItems(@Param("id") Long id);
}
