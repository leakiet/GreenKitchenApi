package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderCode(String orderCode);
}
