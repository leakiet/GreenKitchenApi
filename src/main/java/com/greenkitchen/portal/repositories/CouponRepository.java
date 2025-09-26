package com.greenkitchen.portal.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.Coupon;
import com.greenkitchen.portal.enums.CouponStatus;
import com.greenkitchen.portal.enums.CouponApplicability;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    // Tìm coupon theo code
    Optional<Coupon> findByCode(String code);
    
    // Lấy danh sách coupon có thể đổi (chỉ cần ACTIVE)
    List<Coupon> findByStatusAndIsDeletedFalse(CouponStatus status);
    
    // Lấy danh sách coupon có thể đổi (ACTIVE, không bị xóa, và là GENERAL)
    List<Coupon> findByStatusAndIsDeletedFalseAndApplicability(CouponStatus status, CouponApplicability applicability);
    
    // Lấy danh sách coupon còn hạn và có thể đổi
    @Query("SELECT c FROM Coupon c WHERE c.status = :status AND c.isDeleted = false " +
           "AND c.applicability = :applicability AND c.validUntil > :now " +
           "AND (c.exchangeLimit IS NULL OR c.exchangeCount < c.exchangeLimit)")
    List<Coupon> findAvailableCouponsForExchange(@Param("status") CouponStatus status, 
                                                @Param("applicability") CouponApplicability applicability,
                                                @Param("now") LocalDateTime now);
    
    // Tìm coupon theo points required range
    @Query("SELECT c FROM Coupon c WHERE c.pointsRequired >= :minPoints " +
           "AND c.pointsRequired <= :maxPoints AND c.isDeleted = false")
    List<Coupon> findByPointsRequiredBetween(@Param("minPoints") Double minPoints, 
                                           @Param("maxPoints") Double maxPoints);
}
