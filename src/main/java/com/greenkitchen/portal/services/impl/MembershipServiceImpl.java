package com.greenkitchen.portal.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerMembership;
import com.greenkitchen.portal.entities.PointHistory;
import com.greenkitchen.portal.enums.MembershipTier;
import com.greenkitchen.portal.enums.PointTransactionType;
import com.greenkitchen.portal.repositories.CustomerMembershipRepository;
import com.greenkitchen.portal.repositories.PointHistoryRepository;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.services.MembershipService;

@Service
@Transactional
public class MembershipServiceImpl implements MembershipService {
    
    @Autowired
    private CustomerMembershipRepository membershipRepository;
    
    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private CustomerService customerService;
    
    /**
     * Tính toán và cập nhật membership khi customer có giao dịch mới
     */
    @Override
    public CustomerMembership updateMembershipAfterPurchase(long customerId, double spentAmount, double pointEarned, long orderId) {
        Customer customer = customerService.findById(customerId);
        // Lấy hoặc tạo membership
        CustomerMembership membership = getOrCreateMembership(customer);  
        
        // Tạo point history
        PointHistory pointHistory = new PointHistory(customer, spentAmount, pointEarned,
            "Earned from order: " + orderId, String.valueOf(orderId));
        pointHistoryRepository.save(pointHistory);
        
        // Cập nhật membership
        membership.setTotalPointsEarned(membership.getTotalPointsEarned() + pointEarned);
        membership.setAvailablePoints(membership.getAvailablePoints() + pointEarned);

        // Tính lại tổng chi tiêu 6 tháng qua
        Double totalSpent6Months = calculateTotalSpentLast6Months(customer);
        membership.setTotalSpentLast6Months(totalSpent6Months);
        
        // Cập nhật tier
        MembershipTier newTier = MembershipTier.getTierBySpending(totalSpent6Months.longValue());
        if (newTier != membership.getCurrentTier()) {
            membership.setCurrentTier(newTier);
            membership.setTierAchievedAt(LocalDateTime.now());
        }
        
        membership.setLastUpdatedAt(LocalDateTime.now());
        
        return membershipRepository.save(membership);
    }
    
    /**
     * Lấy thông tin membership hiện tại của customer
     */
    @Override
    public CustomerMembership getCurrentMembership(Customer customer) {
        CustomerMembership membership = getOrCreateMembership(customer);
        
        // Cập nhật điểm còn lại (loại bỏ điểm hết hạn)
        updateAvailablePoints(membership);
        
        // Cập nhật tier dựa trên chi tiêu 6 tháng qua
        Double totalSpent6Months = calculateTotalSpentLast6Months(customer);
        membership.setTotalSpentLast6Months(totalSpent6Months);
        
        MembershipTier currentTier = MembershipTier.getTierBySpending(totalSpent6Months.longValue());
        if (currentTier != membership.getCurrentTier()) {
            membership.setCurrentTier(currentTier);
            membership.setTierAchievedAt(LocalDateTime.now());
        }
        
        membership.setLastUpdatedAt(LocalDateTime.now());
        
        return membershipRepository.save(membership);
    }
    
    @Override
    public boolean usePoints(Customer customer, Double pointsToUse, String description) {
        CustomerMembership membership = getOrCreateMembership(customer);
        updateAvailablePoints(membership);
        
        if (membership.getAvailablePoints() < pointsToUse) {
            return false; // Không đủ điểm
        }
        
        // Tạo point history cho việc sử dụng điểm
        PointHistory pointHistory = new PointHistory();
        pointHistory.setCustomer(customer);
        pointHistory.setSpentAmount(0.0);
        pointHistory.setPointsEarned(-pointsToUse); // Âm vì đang sử dụng
        pointHistory.setEarnedAt(LocalDateTime.now());
        pointHistory.setExpiresAt(LocalDateTime.now().plusYears(1)); // Không hết hạn cho record sử dụng
        pointHistory.setTransactionType(PointTransactionType.USED);
        pointHistory.setDescription(description);
        pointHistory.setIsExpired(false);
        
        pointHistoryRepository.save(pointHistory);
        
        // Cập nhật membership
        membership.setAvailablePoints(membership.getAvailablePoints() - pointsToUse);
        membership.setTotalPointsUsed(membership.getTotalPointsUsed() + pointsToUse);
        membership.setLastUpdatedAt(LocalDateTime.now());
        
        membershipRepository.save(membership);
        
        return true;
    }
    
    /**
     * Lấy hạng membership hiện tại
     */
    @Override
    public MembershipTier getCustomerTier(Customer customer) {
        CustomerMembership membership = getCurrentMembership(customer);
        return membership.getCurrentTier();
    }
    
    /**
     * Lấy hoặc tạo membership cho customer
     */
    private CustomerMembership getOrCreateMembership(Customer customer) {
        return membershipRepository.findByCustomer(customer)
            .orElseGet(() -> {
                CustomerMembership newMembership = new CustomerMembership(customer);
                return membershipRepository.save(newMembership);
            });
    }
    
    /**
     * Tính tổng chi tiêu trong 6 tháng qua
     */
    private Double calculateTotalSpentLast6Months(Customer customer) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<PointHistory> recentTransactions = pointHistoryRepository
            .findByCustomerAndEarnedAtAfterAndTransactionType(customer, sixMonthsAgo, PointTransactionType.EARNED);
        
        return recentTransactions.stream()
            .mapToDouble(PointHistory::getSpentAmount)
            .sum();
    }
    
    /**
     * Cập nhật điểm còn lại (loại bỏ điểm hết hạn)
     */
    private void updateAvailablePoints(CustomerMembership membership) {
        LocalDateTime now = LocalDateTime.now();
        
        // Tìm các điểm đã hết hạn nhưng chưa được đánh dấu
        List<PointHistory> expiredPoints = pointHistoryRepository
            .findByCustomerAndExpiresAtBeforeAndIsExpiredFalseAndTransactionType(
                membership.getCustomer(), now, PointTransactionType.EARNED);
        
        Double totalExpiredPoints = 0.0;
        
        for (PointHistory expiredPoint : expiredPoints) {
            expiredPoint.setIsExpired(true);
            
            // Tạo record cho điểm hết hạn
            PointHistory expiredRecord = new PointHistory();
            expiredRecord.setCustomer(membership.getCustomer());
            expiredRecord.setSpentAmount(0.0);
            expiredRecord.setPointsEarned(-expiredPoint.getPointsEarned());
            expiredRecord.setEarnedAt(now);
            expiredRecord.setExpiresAt(now);
            expiredRecord.setTransactionType(PointTransactionType.EXPIRED);
            expiredRecord.setDescription("Points expired from transaction: " + expiredPoint.getId());
            expiredRecord.setIsExpired(true);
            
            pointHistoryRepository.save(expiredRecord);
            totalExpiredPoints += expiredPoint.getPointsEarned();
        }
        
        pointHistoryRepository.saveAll(expiredPoints);
        
        // Cập nhật available points
        if (totalExpiredPoints > 0.0) {
            membership.setAvailablePoints(
                membership.getAvailablePoints() - totalExpiredPoints);
        }
    }
}
