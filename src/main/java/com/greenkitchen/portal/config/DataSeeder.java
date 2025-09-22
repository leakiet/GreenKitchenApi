package com.greenkitchen.portal.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.greenkitchen.portal.entities.Coupon;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerCoupon;
import com.greenkitchen.portal.enums.CouponApplicability;
import com.greenkitchen.portal.enums.CouponStatus;
import com.greenkitchen.portal.enums.CouponType;
import com.greenkitchen.portal.enums.CustomerCouponStatus;
import com.greenkitchen.portal.repositories.CouponRepository;
import com.greenkitchen.portal.repositories.CustomerCouponRepository;
import com.greenkitchen.portal.repositories.CustomerRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerCouponRepository customerCouponRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create test coupons
        createTestCoupons();

        // Create test customer coupons
        createTestCustomerCoupons();
    }

    private void createTestCoupons() {
        if (couponRepository.count() == 0) {
            // Create percentage coupon
            Coupon percentCoupon = new Coupon();
            percentCoupon.setCode("SAVE10");
            percentCoupon.setName("Giảm 10%");
            percentCoupon.setDescription("Giảm 10% cho đơn hàng từ 100k");
            percentCoupon.setType(CouponType.PERCENTAGE);
            percentCoupon.setDiscountValue(10.0);
            percentCoupon.setPointsRequired(50.0);
            percentCoupon.setValidUntil(LocalDateTime.now().plusMonths(6));
            percentCoupon.setStatus(CouponStatus.ACTIVE);
            percentCoupon.setApplicability(CouponApplicability.GENERAL);
            percentCoupon.setCreatedAt(LocalDateTime.now());
            percentCoupon.setUpdatedAt(LocalDateTime.now());
            percentCoupon.setIsDeleted(false);
            couponRepository.save(percentCoupon);

            // Create fixed amount coupon
            Coupon fixedCoupon = new Coupon();
            fixedCoupon.setCode("FIXED50K");
            fixedCoupon.setName("Giảm 50k");
            fixedCoupon.setDescription("Giảm 50k cho đơn hàng từ 200k");
            fixedCoupon.setType(CouponType.FIXED_AMOUNT);
            fixedCoupon.setDiscountValue(50000.0);
            fixedCoupon.setPointsRequired(100.0);
            fixedCoupon.setValidUntil(LocalDateTime.now().plusMonths(6));
            fixedCoupon.setStatus(CouponStatus.ACTIVE);
            fixedCoupon.setApplicability(CouponApplicability.GENERAL);
            fixedCoupon.setCreatedAt(LocalDateTime.now());
            fixedCoupon.setUpdatedAt(LocalDateTime.now());
            fixedCoupon.setIsDeleted(false);
            couponRepository.save(fixedCoupon);

            System.out.println("Created test coupons");
        }
    }

    private void createTestCustomerCoupons() {
        if (customerCouponRepository.count() == 0) {
            // Get first customer (assuming customer with ID 1 exists)
            Customer customer = customerRepository.findById(1L).orElse(null);
            if (customer != null) {
                // Get all coupons
                Iterable<Coupon> coupons = couponRepository.findAll();
                for (Coupon coupon : coupons) {
                    CustomerCoupon customerCoupon = new CustomerCoupon();
                    customerCoupon.setCustomer(customer);
                    customerCoupon.setCoupon(coupon);
                    customerCoupon.setExchangedAt(LocalDateTime.now());
                    customerCoupon.setExpiresAt(coupon.getValidUntil());
                    customerCoupon.setStatus(CustomerCouponStatus.AVAILABLE);

                    // Snapshot thông tin coupon
                    customerCoupon.setCouponCode(coupon.getCode());
                    customerCoupon.setCouponName(coupon.getName());
                    customerCoupon.setCouponDescription(coupon.getDescription());
                    customerCoupon.setCouponType(coupon.getType());
                    customerCoupon.setCouponApplicability(coupon.getApplicability());
                    customerCoupon.setCouponDiscountValue(coupon.getDiscountValue());
                    customerCoupon.setMaxDiscount(coupon.getMaxDiscount());
                    customerCoupon.setPointsRequired(coupon.getPointsRequired());

                    customerCoupon.setCreatedAt(LocalDateTime.now());
                    customerCoupon.setUpdatedAt(LocalDateTime.now());
                    customerCoupon.setIsDeleted(false);

                    customerCouponRepository.save(customerCoupon);
                }
                System.out.println("Created test customer coupons");
            }
        }
    }
}