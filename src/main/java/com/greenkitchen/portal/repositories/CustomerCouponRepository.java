package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.CustomerCoupon;

@Repository
public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, Long> {
    
    // Kiểm tra customer đã có coupon này chưa (để tránh đổi trùng)
    @Query("SELECT COUNT(cc) > 0 FROM CustomerCoupon cc WHERE cc.customer.id = :customerId " +
           "AND cc.coupon.id = :couponId AND cc.isDeleted = false")
    boolean existsByCustomerIdAndCouponId(@Param("customerId") Long customerId, 
                                        @Param("couponId") Long couponId);
    
    // Tìm tất cả customer IDs theo coupon ID
    @Query("SELECT cc.customer.id FROM CustomerCoupon cc WHERE cc.coupon.id = :couponId AND cc.isDeleted = false")
    java.util.List<Long> findCustomerIdsByCouponId(@Param("couponId") Long couponId);
    
    // Lấy danh sách customer coupons có sẵn cho customer
    @Query("SELECT cc FROM CustomerCoupon cc WHERE cc.customer.id = :customerId " +
           "AND cc.status = :status AND cc.expiresAt > :now AND cc.isDeleted = false " +
           "AND cc.couponApplicability = :applicability")
    java.util.List<CustomerCoupon> findAvailableCustomerCoupons(
        @Param("customerId") Long customerId,
        @Param("status") com.greenkitchen.portal.enums.CustomerCouponStatus status,
        @Param("now") java.time.LocalDateTime now,
        @Param("applicability") com.greenkitchen.portal.enums.CouponApplicability applicability
    );
    
    // Tìm customer coupon theo customer ID và coupon code
    java.util.Optional<CustomerCoupon> findByCustomerIdAndCouponCodeAndIsDeletedFalse(
        Long customerId, String couponCode
    );
}
