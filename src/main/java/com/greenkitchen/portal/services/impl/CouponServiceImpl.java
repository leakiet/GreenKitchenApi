package com.greenkitchen.portal.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.Coupon;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerCoupon;
import com.greenkitchen.portal.entities.CustomerMembership;
import com.greenkitchen.portal.entities.PointHistory;
import com.greenkitchen.portal.enums.CouponStatus;
import com.greenkitchen.portal.enums.CustomerCouponStatus;
import com.greenkitchen.portal.enums.PointTransactionType;
import com.greenkitchen.portal.repositories.CouponRepository;
import com.greenkitchen.portal.repositories.CustomerCouponRepository;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.PointHistoryRepository;
import com.greenkitchen.portal.services.CouponService;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CustomerCouponRepository customerCouponRepository;
    
    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Override
    public List<Coupon> getAvailableCouponsForExchange() {
        return couponRepository.findByStatusAndIsDeletedFalse(CouponStatus.ACTIVE);
    }

    @Override
    @Transactional
    public Coupon exchangePointsForCoupon(Long customerId, Long couponId) {
        // Lấy thông tin customer
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        // Lấy thông tin coupon
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        
        // Kiểm tra coupon có thể đổi không
        if (!isValidForExchange(coupon)) {
            throw new IllegalStateException("Coupon không thể đổi");
        }
        
        // Kiểm tra điểm của customer
        CustomerMembership membership = customer.getMembership();
        if (membership == null) {
            throw new IllegalStateException("Customer chưa có thông tin membership");
        }
        
        Double availablePoints = membership.getAvailablePoints();
        if (availablePoints < coupon.getPointsRequired()) {
            throw new IllegalStateException("Không đủ điểm để đổi coupon này");
        }

        // Lưu PointHistory để ghi nhận việc sử dụng điểm
        createPointHistory(customer, coupon.getPointsRequired(), coupon.getName());
        
        // Trừ điểm của customer
        Double newPoints = availablePoints - coupon.getPointsRequired();
        membership.setAvailablePoints(newPoints);
        membership.setTotalPointsUsed(membership.getTotalPointsUsed() + coupon.getPointsRequired());
        membership.setUpdatedAt(LocalDateTime.now());
        
        // Tạo CustomerCoupon mới
        CustomerCoupon customerCoupon = createCustomerCoupon(customer, coupon);
        customerCouponRepository.save(customerCoupon);
  
        
        // Tăng exchange count của coupon
        coupon.setExchangeCount(coupon.getExchangeCount() + 1);
        coupon.setUpdatedAt(LocalDateTime.now());
        
        return couponRepository.save(coupon);
    }

    @Override
    public Coupon getCouponById(Long couponId) {
        return couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
    }

    @Override
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
    }
    
    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }
    
    @Override
    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        // Validate coupon code uniqueness
        if (couponRepository.findByCode(coupon.getCode()).isPresent()) {
            throw new IllegalArgumentException("Coupon code already exists");
        }
        
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());
        coupon.setIsDeleted(false);
        
        return couponRepository.save(coupon);
    }
    
    @Override
    @Transactional
    public Coupon updateCoupon(Long couponId, Coupon coupon) {
        Coupon existingCoupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        
        // Check if code is being changed and if it's unique
        if (!existingCoupon.getCode().equals(coupon.getCode()) && 
            couponRepository.findByCode(coupon.getCode()).isPresent()) {
            throw new IllegalArgumentException("Coupon code already exists");
        }
        
        // Update fields
        existingCoupon.setCode(coupon.getCode());
        existingCoupon.setName(coupon.getName());
        existingCoupon.setDescription(coupon.getDescription());
        existingCoupon.setType(coupon.getType());
        existingCoupon.setDiscountValue(coupon.getDiscountValue());
        existingCoupon.setMaxDiscount(coupon.getMaxDiscount());
        existingCoupon.setPointsRequired(coupon.getPointsRequired());
        existingCoupon.setValidUntil(coupon.getValidUntil());
        existingCoupon.setExchangeLimit(coupon.getExchangeLimit());
        existingCoupon.setStatus(coupon.getStatus());
        existingCoupon.setUpdatedAt(LocalDateTime.now());
        
        return couponRepository.save(existingCoupon);
    }
    
    @Override
    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        
        // Soft delete
        coupon.setIsDeleted(true);
        coupon.setUpdatedAt(LocalDateTime.now());
        couponRepository.save(coupon);
    }
    
    // Helper methods
    private boolean isValidForExchange(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        
        // Kiểm tra trạng thái
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            return false;
        }
        
        // Kiểm tra thời gian hết hạn
        if (coupon.getValidUntil().isBefore(now)) {
            return false;
        }
        
        // Kiểm tra giới hạn đổi
        if (coupon.getExchangeLimit() != null && 
            coupon.getExchangeCount() >= coupon.getExchangeLimit()) {
            return false;
        }
        
        return true;
    }
    
    private CustomerCoupon createCustomerCoupon(Customer customer, Coupon coupon) {
        CustomerCoupon customerCoupon = new CustomerCoupon();
        customerCoupon.setCustomer(customer);
        customerCoupon.setCoupon(coupon);
        customerCoupon.setExchangedAt(LocalDateTime.now());
        customerCoupon.setExpiresAt(coupon.getValidUntil());
        customerCoupon.setStatus(CustomerCouponStatus.AVAILABLE);
        
        // Snapshot thông tin coupon tại thời điểm đổi
        customerCoupon.setCouponCode(coupon.getCode());
        customerCoupon.setCouponName(coupon.getName());
        customerCoupon.setCouponDescription(coupon.getDescription());
        customerCoupon.setCouponType(coupon.getType());
        customerCoupon.setCouponDiscountValue(coupon.getDiscountValue());
        customerCoupon.setMaxDiscount(coupon.getMaxDiscount());
        
        customerCoupon.setCreatedAt(LocalDateTime.now());
        customerCoupon.setUpdatedAt(LocalDateTime.now());
        customerCoupon.setIsDeleted(false);
        
        return customerCoupon;
    }
    
    private void createPointHistory(Customer customer, Double pointsUsed, String couponName) {
        PointHistory pointHistory = new PointHistory();
        pointHistory.setCustomer(customer);
        pointHistory.setSpentAmount(0.0); // Không có tiền chi tiêu
        pointHistory.setPointsEarned(-pointsUsed); // Điểm âm = điểm đã sử dụng
        pointHistory.setEarnedAt(LocalDateTime.now());
        pointHistory.setExpiresAt(LocalDateTime.now().plusYears(1)); // Lịch sử không hết hạn
        pointHistory.setTransactionType(PointTransactionType.USED);
        pointHistory.setDescription("Đổi điểm lấy coupon: " + couponName);
        pointHistory.setOrderId(null);
        pointHistory.setIsExpired(false);
        pointHistory.setCreatedAt(LocalDateTime.now());
        pointHistory.setUpdatedAt(LocalDateTime.now());
        pointHistory.setIsDeleted(false);
        
        pointHistoryRepository.save(pointHistory);
    }
}
