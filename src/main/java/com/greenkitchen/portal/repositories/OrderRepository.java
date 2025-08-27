package com.greenkitchen.portal.repositories;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.greenkitchen.portal.enums.OrderStatus;
import com.greenkitchen.portal.entities.Order;


public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderCode(String orderCode);

    @Query("SELECT o FROM Order o WHERE COALESCE(o.isDeleted, false) = false " +
        "AND (:status IS NULL OR o.status = :status) " +
        "AND (:q IS NULL OR o.orderCode LIKE CONCAT('%', :q, '%') OR o.recipientName LIKE CONCAT('%', :q, '%')) " +
        "AND (:fromDate IS NULL OR o.createdAt >= :fromDate) " +
        "AND (:toDate IS NULL OR o.createdAt <= :toDate) " +
        "ORDER BY o.createdAt DESC")
    Page<Order> findFilteredPaged(@Param("status") OrderStatus status,
                                  @Param("q") String q,
                                  @Param("fromDate") java.time.LocalDateTime fromDate,
                                  @Param("toDate") java.time.LocalDateTime toDate,
                                  Pageable pageable);
}
