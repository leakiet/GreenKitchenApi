package com.greenkitchen.portal.repositories;

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
    
    // Tìm coupon theo points required range
    @Query("SELECT c FROM Coupon c WHERE c.pointsRequired >= :minPoints " +
           "AND c.pointsRequired <= :maxPoints AND c.isDeleted = false")
    List<Coupon> findByPointsRequiredBetween(@Param("minPoints") Double minPoints, 
                                           @Param("maxPoints") Double maxPoints);
}
