package com.greenkitchen.portal.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.CartScanLog;

@Repository
public interface CartScanLogRepository extends JpaRepository<CartScanLog, Long> {
    
    /**
     * Tìm log quét cart theo customerId
     */
    Optional<CartScanLog> findByCustomerId(Long customerId);
    
    /**
     * Kiểm tra customer đã được quét chưa
     */
    boolean existsByCustomerId(Long customerId);
    
    /**
     * Lấy danh sách customerId đã được quét
     */
    @Query("SELECT csl.customerId FROM CartScanLog csl WHERE csl.isDeleted = false")
    List<Long> findAllScannedCustomerIds();
    
    /**
     * Lấy danh sách customerId đã được quét theo scan type
     */
    @Query("SELECT csl.customerId FROM CartScanLog csl WHERE csl.scanType = :scanType AND csl.isDeleted = false")
    List<Long> findScannedCustomerIdsByScanType(@Param("scanType") String scanType);
    
    /**
     * Lấy log quét cart theo customerId và scan type
     */
    Optional<CartScanLog> findByCustomerIdAndScanType(Long customerId, String scanType);
}
