package com.greenkitchen.portal.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.PointHistory;
import com.greenkitchen.portal.enums.PointTransactionType;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    
    List<PointHistory> findByCustomerAndEarnedAtAfterAndTransactionType(
        Customer customer, LocalDateTime after, PointTransactionType transactionType);
    
    List<PointHistory> findByCustomerAndExpiresAtBeforeAndIsExpiredFalseAndTransactionType(
        Customer customer, LocalDateTime before, PointTransactionType transactionType);
    
    List<PointHistory> findByCustomerOrderByEarnedAtDesc(Customer customer);
}
