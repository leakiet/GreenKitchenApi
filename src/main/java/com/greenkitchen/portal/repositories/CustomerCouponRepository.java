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
}
