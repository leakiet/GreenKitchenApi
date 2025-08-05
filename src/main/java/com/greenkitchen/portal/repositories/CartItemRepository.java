package com.greenkitchen.portal.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Tìm cart items không bị xóa
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.isDeleted = false")
    List<CartItem> findByCartIdAndNotDeleted(@Param("cartId") Long cartId);
    
    // Tìm cart item theo ID và không bị xóa
    @Query("SELECT ci FROM CartItem ci WHERE ci.id = :id AND ci.isDeleted = false")
    Optional<CartItem> findByIdAndNotDeleted(@Param("id") Long id);
    
    // Soft delete tất cả items của cart
    @Query("UPDATE CartItem ci SET ci.isDeleted = true WHERE ci.cart.id = :cartId")
    void softDeleteByCartId(@Param("cartId") Long cartId);
}
