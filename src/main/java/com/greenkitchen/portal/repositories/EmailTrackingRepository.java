package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.EmailTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTrackingRepository extends JpaRepository<EmailTracking, Long> {
    
    Optional<EmailTracking> findByTrackingId(String trackingId);
    
    List<EmailTracking> findByCustomerIdAndEmailType(Long customerId, String emailType);
    
    List<EmailTracking> findByEmailTypeAndClickedAtBetween(String emailType, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(e) FROM EmailTracking e WHERE e.emailType = :emailType AND e.clickedAt IS NOT NULL")
    Long countClicksByEmailType(@Param("emailType") String emailType);
    
    @Query("SELECT COUNT(e) FROM EmailTracking e WHERE e.emailType = :emailType AND e.clickedAt IS NOT NULL AND e.clickedAt >= :startDate")
    Long countClicksByEmailTypeSince(@Param("emailType") String emailType, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT e.linkType, COUNT(e) FROM EmailTracking e WHERE e.emailType = :emailType AND e.clickedAt IS NOT NULL GROUP BY e.linkType")
    List<Object[]> getClickStatsByLinkType(@Param("emailType") String emailType);
    
    // Thêm các method cho phân trang
    Page<EmailTracking> findByCustomerId(Long customerId, Pageable pageable);
    
    Page<EmailTracking> findByEmailType(String emailType, Pageable pageable);
    
    Page<EmailTracking> findByLinkType(String linkType, Pageable pageable);
    
    // Đếm theo link type
    long countByLinkType(String linkType);
    
    // Tìm theo customerId và linkType
    Page<EmailTracking> findByCustomerIdAndLinkType(Long customerId, String linkType, Pageable pageable);
    
    // Tìm theo emailType và linkType
    Page<EmailTracking> findByEmailTypeAndLinkType(String emailType, String linkType, Pageable pageable);

    // Only clicks: clickedAt IS NOT NULL
    Page<EmailTracking> findByCustomerIdAndLinkTypeAndClickedAtIsNotNull(Long customerId, String linkType, Pageable pageable);
    Page<EmailTracking> findByEmailTypeAndLinkTypeAndClickedAtIsNotNull(String emailType, String linkType, Pageable pageable);
    Page<EmailTracking> findByLinkTypeAndClickedAtIsNotNull(String linkType, Pageable pageable);
    long countByLinkTypeAndClickedAtIsNotNull(String linkType);
}
