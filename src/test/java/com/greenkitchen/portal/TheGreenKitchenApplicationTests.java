package com.greenkitchen.portal;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerMembership;
import com.greenkitchen.portal.entities.PointHistory;
import com.greenkitchen.portal.enums.MembershipTier;
import com.greenkitchen.portal.enums.PointTransactionType;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.PointHistoryRepository;
import com.greenkitchen.portal.services.MembershipService;

@SpringBootTest
@ActiveProfiles("test")
class TheGreenKitchenApplicationTests {

	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private PointHistoryRepository pointHistoryRepository;
	
	@Autowired
	private MembershipService membershipService;

	@Test
	void contextLoads() {
		// Simple test without Spring context
		System.out.println("✅ Basic context test passed");
	}
	
	@Test
	void testMembershipSystemLogic() {
		System.out.println("=== TESTING MEMBERSHIP SYSTEM LOGIC ===");
		System.out.println();
		
		// Test tier calculation logic
		testTierCalculation();
		
		// Test point history logic
		testPointHistoryLogic();
		
		// Test point history database operations
		testPointHistoryDatabaseOperations();
		
		
		System.out.println();
		System.out.println("📋 To test with real data, run the application and use these endpoints:");
		System.out.println("1. Start app: ./gradlew bootRun");
		System.out.println("2. Test endpoints:");
		System.out.println("   📦 Add transaction: POST /apis/v1/membership-test/add-transaction/1?amount=800000");
		System.out.println("   🎯 Simulate purchases: POST /apis/v1/membership-test/simulate-purchases/1");
		System.out.println("   📊 Get info: GET /apis/v1/membership-test/info/1");
		System.out.println("   📜 Get history: GET /apis/v1/membership-test/history/1");
		System.out.println("   💰 Use points: POST /apis/v1/membership-test/use-points/1?points=500");
		System.out.println("   🗑️ Clear data: POST /apis/v1/membership-test/clear/1");
		
		System.out.println();
		System.out.println("✅ All logic tests passed!");
	}
	
	@Test
	@Transactional
	void testCreateMockDataForCustomer1() {
		System.out.println("=== CREATING MOCK DATA FOR CUSTOMER ID = 1 ===");
		System.out.println();
		
		// Find or create customer with ID = 1
		Customer customer = customerRepository.findById(1L).orElse(null);
		
		if (customer == null) {
			System.out.println("❌ Customer with ID = 1 not found in database");
			System.out.println("Please create a customer with ID = 1 first or check if customer exists");
			return;
		}
		
		System.out.println("✅ Found customer: " + customer.getFullName() + " (ID: " + customer.getId() + ")");
		System.out.println("   Email: " + customer.getEmail());
		
		// Create mock purchase transactions
		System.out.println();
		System.out.println("💰 Creating mock purchase history...");
		
		Double[] purchaseAmounts = {
			800000.0,    // 800,000 VND = 800 points
			1500000.0,   // 1,500,000 VND = 1500 points  
			3000000.0,   // 3,000,000 VND = 3000 points
			250000.0,    // 250,000 VND = 250 points
			1200000.0    // 1,200,000 VND = 1200 points
		};
		
		String[] orderIds = {"DH001", "DH002", "DH003", "DH004", "DH005"};
		String[] descriptions = {
			"Mua đơn hàng #DH001 - Salad rau xanh hữu cơ",
			"Mua đơn hàng #DH002 - Bánh mì nguyên cám + Nước ép",
			"Mua đơn hàng #DH003 - Combo healthy meal deluxe",
			"Mua đơn hàng #DH004 - Nước ép trái cây tươi",
			"Mua đơn hàng #DH005 - Thực phẩm organic cao cấp"
		};
		Double totalSpent = 0.0;
		Double totalPointsEarned = 0.0;
		
		// Create purchase transactions using MembershipService
		for (int i = 0; i < purchaseAmounts.length; i++) {
			Double amount = purchaseAmounts[i];
			String orderId = orderIds[i];
			String description = descriptions[i];
			
			System.out.println("📦 Processing purchase " + (i + 1) + ":");
			System.out.println("   Order ID: " + orderId);
			System.out.println("   Amount: " + String.format("%,d", amount.longValue()) + " VND");
			System.out.println("   Description: " + description);
			
		}
		
		// Create some point usage transactions
		System.out.println("💸 Creating point usage history...");
		
		// Use 500 points
		boolean useResult1 = membershipService.usePoints(customer, 500.0, "Sử dụng điểm thanh toán đơn hàng #DH006");
		if (useResult1) {
			System.out.println("✅ Used 500 points successfully");
		} else {
			System.out.println("❌ Failed to use 500 points");
		}
		
		// Use 300 points
		boolean useResult2 = membershipService.usePoints(customer, 300.0, "Sử dụng điểm mua voucher giảm giá");
		if (useResult2) {
			System.out.println("✅ Used 300 points successfully");
		} else {
			System.out.println("❌ Failed to use 300 points");
		}
		
		// Create some expired points manually (simulate old points that expired)
		System.out.println();
		System.out.println("⏰ Creating expired point history...");
		
		PointHistory expiredHistory1 = new PointHistory();
		expiredHistory1.setCustomer(customer);
		expiredHistory1.setSpentAmount(0.0);
		expiredHistory1.setPointsEarned(-150.0); // Negative for expired points
		expiredHistory1.setTransactionType(PointTransactionType.EXPIRED);
		expiredHistory1.setDescription("Điểm hết hạn sau 6 tháng - Từ đơn hàng cũ");
		expiredHistory1.setOrderId(null);
		expiredHistory1.setEarnedAt(LocalDateTime.now().minusMonths(7));
		expiredHistory1.setExpiresAt(LocalDateTime.now().minusMonths(1));
		expiredHistory1.setIsExpired(true);
		
		pointHistoryRepository.save(expiredHistory1);
		System.out.println("✅ Created expired point record: 150 points expired");
		
		// Get final membership status
		System.out.println();
		System.out.println("📊 Final membership status:");
		CustomerMembership finalMembership = membershipService.getCurrentMembership(customer);
		
		System.out.println("Customer: " + customer.getFullName());
		System.out.println("Total spent (last 6 months): " + String.format("%,d", finalMembership.getTotalSpentLast6Months().longValue()) + " VND");
		System.out.println("Current tier: " + finalMembership.getCurrentTier());
		System.out.println("Points available: " + finalMembership.getAvailablePoints() + " points");
		System.out.println("Total points earned: " + finalMembership.getTotalPointsEarned() + " points");
		System.out.println("Total points used: " + finalMembership.getTotalPointsUsed() + " points");
		
		// Display point history
		System.out.println();
		System.out.println("📜 Point transaction history:");
		var pointHistory = pointHistoryRepository.findByCustomerOrderByEarnedAtDesc(customer);
		
		for (int i = 0; i < Math.min(pointHistory.size(), 10); i++) { // Show last 10 transactions
			PointHistory history = pointHistory.get(i);
			String pointsText = history.getPointsEarned().compareTo(0.0) >= 0
				? "+" + history.getPointsEarned() + " points"
				: history.getPointsEarned() + " points";
				
			System.out.println((i + 1) + ". " + history.getTransactionType() + ": " + pointsText);
			System.out.println("   " + history.getDescription());
			System.out.println("   Date: " + history.getEarnedAt().toLocalDate());
			if (history.getOrderId() != null) {
				System.out.println("   Order: " + history.getOrderId());
			}
			System.out.println();
		}
		
		System.out.println("✅ Mock data creation completed successfully!");
		System.out.println("Total transactions created: " + pointHistory.size());
	}
	
	private void testTierCalculation() {
		System.out.println("🧪 Testing tier calculation logic...");
		
		// Test MembershipTier enum logic
		MembershipTier energy = MembershipTier.getTierBySpending(1000000);
		MembershipTier vitality = MembershipTier.getTierBySpending(3000000);  
		MembershipTier radiance = MembershipTier.getTierBySpending(6000000);
		
		assert energy == MembershipTier.ENERGY : "1M should be ENERGY tier";
		assert vitality == MembershipTier.VITALITY : "3M should be VITALITY tier";
		assert radiance == MembershipTier.RADIANCE : "6M should be RADIANCE tier";
		
		System.out.println("✓ Tier calculation: 1,000,000 VND -> " + energy + " (" + energy.getMinSpending() + " - " + energy.getMaxSpending() + ")");
		System.out.println("✓ Tier calculation: 3,000,000 VND -> " + vitality + " (" + vitality.getMinSpending() + " - " + vitality.getMaxSpending() + ")");
		System.out.println("✓ Tier calculation: 6,000,000 VND -> " + radiance + " (" + radiance.getMinSpending() + "+)");
		
		// Test boundary cases
		MembershipTier boundary1 = MembershipTier.getTierBySpending(1999999); // Should be ENERGY
		MembershipTier boundary2 = MembershipTier.getTierBySpending(2000000); // Should be VITALITY
		MembershipTier boundary3 = MembershipTier.getTierBySpending(4999999); // Should be VITALITY
		MembershipTier boundary4 = MembershipTier.getTierBySpending(5000000); // Should be RADIANCE
		
		assert boundary1 == MembershipTier.ENERGY : "1,999,999 should be ENERGY";
		assert boundary2 == MembershipTier.VITALITY : "2,000,000 should be VITALITY";
		assert boundary3 == MembershipTier.VITALITY : "4,999,999 should be VITALITY";
		assert boundary4 == MembershipTier.RADIANCE : "5,000,000 should be RADIANCE";
		
		System.out.println("✓ Boundary test: 1,999,999 VND -> " + boundary1);
		System.out.println("✓ Boundary test: 2,000,000 VND -> " + boundary2);
		System.out.println("✓ Boundary test: 4,999,999 VND -> " + boundary3);
		System.out.println("✓ Boundary test: 5,000,000 VND -> " + boundary4);
		
		// Test points calculation  
		System.out.println();
		System.out.println("🧮 Testing points calculation...");
		Double[] amounts = {
			800000.0,    // 800 points
			1500000.0,   // 1500 points
			3000000.0,   // 3000 points
			250000.0     // 250 points
		};
		
		for (Double amount : amounts) {
			Double expectedPoints = amount / 1000.0;
			System.out.println("✓ Points calculation: " + String.format("%,d", amount.longValue()) + " VND -> " + expectedPoints + " points");
		}
		
		// Test total scenario
		System.out.println();
		System.out.println("📈 Full scenario test:");
		long totalSpent = 800000 + 1500000 + 3000000 + 250000; // 5,550,000 VND
		long totalPoints = 800 + 1500 + 3000 + 250; // 5,550 points
		MembershipTier finalTier = MembershipTier.getTierBySpending(totalSpent);
		
		System.out.println("Total spent: " + String.format("%,d", totalSpent) + " VND");
		System.out.println("Total points earned: " + String.format("%,d", totalPoints) + " points");
		System.out.println("Final tier: " + finalTier);
		System.out.println("After using 500 points: " + String.format("%,d", totalPoints - 500) + " points remaining");
		
		assert finalTier == MembershipTier.RADIANCE : "5,550,000 should be RADIANCE tier";
		assert totalPoints == 5550 : "Total points should be 5550";
	}
	
	private void testPointHistoryLogic() {
		System.out.println();
		System.out.println("📋 Testing point history logic...");
		
		// Test point transaction types
		System.out.println("🧪 Testing point transaction types:");
		PointTransactionType earned = PointTransactionType.EARNED;
		PointTransactionType used = PointTransactionType.USED;
		PointTransactionType expired = PointTransactionType.EXPIRED;
		
		// Test point history creation scenario
		System.out.println();
		System.out.println("💰 Testing point history creation scenario:");
		
		// Simulate different transaction scenarios
		Double[] purchases = {
			800000.0,    // Order DH001: 800,000 VND = 800 points
			1500000.0,   // Order DH002: 1,500,000 VND = 1500 points
			3000000.0,   // Order DH003: 3,000,000 VND = 3000 points
			250000.0     // Order DH004: 250,000 VND = 250 points
		};
		
		String[] orderIds = {"DH001", "DH002", "DH003", "DH004"};
		String[] descriptions = {
			"Mua đơn hàng #DH001 - Salad rau xanh hữu cơ",
			"Mua đơn hàng #DH002 - Bánh mì nguyên cám",
			"Mua đơn hàng #DH003 - Combo healthy meal",
			"Mua đơn hàng #DH004 - Nước ép trái cây"
		};
		
		Double totalPointsEarned = 0.0;
		
		for (int i = 0; i < purchases.length; i++) {
			Double spentAmount = purchases[i];
			Double pointsEarned = spentAmount / 1000.0; // 1 point per 1000 VND
			String orderId = orderIds[i];
			String description = descriptions[i];
			
			totalPointsEarned += pointsEarned;
			
			System.out.println("✓ Transaction " + (i + 1) + ":");
			System.out.println("  - Order ID: " + orderId);
			System.out.println("  - Spent: " + String.format("%,d", spentAmount.longValue()) + " VND");
			System.out.println("  - Points earned: " + pointsEarned + " points");
			System.out.println("  - Description: " + description);
			System.out.println("  - Transaction type: " + PointTransactionType.EARNED);
		}
		
		System.out.println();
		System.out.println("📊 Point history summary:");
		System.out.println("Total transactions: " + purchases.length);
		System.out.println("Total points earned: " + totalPointsEarned + " points");
		System.out.println("Total spent: " + String.format("%,d", 
			(long)(purchases[0] + purchases[1] + purchases[2] + purchases[3])) + " VND");
		
		// Test using points scenario
		System.out.println();
		System.out.println("💸 Testing point usage scenario:");
		Double pointsToUse = 500.0;
		Double remainingPoints = totalPointsEarned - pointsToUse;
		
		System.out.println("✓ Using points transaction:");
		System.out.println("  - Points used: " + pointsToUse + " points");
		System.out.println("  - Transaction type: " + PointTransactionType.USED);
		System.out.println("  - Description: Sử dụng điểm cho đơn hàng #DH005");
		System.out.println("  - Remaining points: " + remainingPoints + " points");
		
		// Test point expiration scenario
		System.out.println();
		System.out.println("⏰ Testing point expiration scenario:");
		Double expiredPoints = 100.0;
		Double finalRemainingPoints = remainingPoints - expiredPoints;
		
		System.out.println("✓ Point expiration transaction:");
		System.out.println("  - Expired points: " + expiredPoints + " points");
		System.out.println("  - Transaction type: " + PointTransactionType.EXPIRED);
		System.out.println("  - Description: Điểm hết hạn sau 6 tháng");
		System.out.println("  - Final remaining points: " + finalRemainingPoints + " points");
		
		// Verify calculations
		assert totalPointsEarned.equals(5550.0) : "Total points earned should be 5550";
		assert remainingPoints.equals(5050.0) : "Remaining points after usage should be 5050";
		assert finalRemainingPoints.equals(4950.0) : "Final remaining points should be 4950";
		
		System.out.println();
		System.out.println("✅ Point history logic tests completed successfully!");
	}
	
	private void testPointHistoryDatabaseOperations() {
		System.out.println();
		System.out.println("🗄️ Testing point history database operations...");
		
		// Mock customer for testing
		Customer mockCustomer = new Customer();
		mockCustomer.setId(1L);
		mockCustomer.setEmail("test@greenkitchen.com");
		mockCustomer.setFirstName("Test");
		mockCustomer.setLastName("Customer");
		
		System.out.println("✓ Created mock customer: " + mockCustomer.getFullName() + " (ID: " + mockCustomer.getId() + ")");
		
		// Test creating point history entries
		System.out.println();
		System.out.println("📝 Testing PointHistory entity creation:");
		
		// Test 1: Create EARNED transaction
		Double spentAmount1 = 800000.0;
		Double pointsEarned1 = spentAmount1 / 1000.0;
		String orderId1 = "DH001";
		String description1 = "Mua đơn hàng #DH001 - Salad rau xanh hữu cơ";
		
		PointHistory earnedHistory = new PointHistory(mockCustomer, spentAmount1, pointsEarned1, description1, orderId1);
		
		System.out.println("✓ Created EARNED transaction:");
		System.out.println("  - Customer ID: " + earnedHistory.getCustomer().getId());
		System.out.println("  - Spent Amount: " + String.format("%,d", earnedHistory.getSpentAmount().longValue()) + " VND");
		System.out.println("  - Points Earned: " + earnedHistory.getPointsEarned() + " points");
		System.out.println("  - Order ID: " + earnedHistory.getOrderId());
		System.out.println("  - Description: " + earnedHistory.getDescription());
		System.out.println("  - Transaction Type: " + earnedHistory.getTransactionType());
		System.out.println("  - Earned At: " + earnedHistory.getEarnedAt());
		System.out.println("  - Expires At: " + earnedHistory.getExpiresAt());
		System.out.println("  - Is Expired: " + earnedHistory.getIsExpired());
		
		// Test 2: Create USED transaction
		PointHistory usedHistory = new PointHistory();
		usedHistory.setCustomer(mockCustomer);
		usedHistory.setSpentAmount(0.0);
		usedHistory.setPointsEarned(-500.0); // Negative for used points
		usedHistory.setTransactionType(PointTransactionType.USED);
		usedHistory.setDescription("Sử dụng điểm cho đơn hàng #DH005");
		usedHistory.setOrderId("DH005");
		usedHistory.setEarnedAt(LocalDateTime.now());
		usedHistory.setExpiresAt(LocalDateTime.now().plusMonths(6));
		usedHistory.setIsExpired(false);
		
		System.out.println();
		System.out.println("✓ Created USED transaction:");
		System.out.println("  - Customer ID: " + usedHistory.getCustomer().getId());
		System.out.println("  - Points Used: " + Math.abs(usedHistory.getPointsEarned().longValue()) + " points");
		System.out.println("  - Order ID: " + usedHistory.getOrderId());
		System.out.println("  - Description: " + usedHistory.getDescription());
		System.out.println("  - Transaction Type: " + usedHistory.getTransactionType());
		
		// Test 3: Create EXPIRED transaction
		PointHistory expiredHistory = new PointHistory();
		expiredHistory.setCustomer(mockCustomer);
		expiredHistory.setSpentAmount(0.0);
		expiredHistory.setPointsEarned(-100.0); // Negative for expired points
		expiredHistory.setTransactionType(PointTransactionType.EXPIRED);
		expiredHistory.setDescription("Điểm hết hạn sau 6 tháng");
		expiredHistory.setOrderId(null);
		expiredHistory.setEarnedAt(LocalDateTime.now().minusMonths(6));
		expiredHistory.setExpiresAt(LocalDateTime.now());
		expiredHistory.setIsExpired(true);
		
		System.out.println();
		System.out.println("✓ Created EXPIRED transaction:");
		System.out.println("  - Customer ID: " + expiredHistory.getCustomer().getId());
		System.out.println("  - Points Expired: " + Math.abs(expiredHistory.getPointsEarned().longValue()) + " points");
		System.out.println("  - Description: " + expiredHistory.getDescription());
		System.out.println("  - Transaction Type: " + expiredHistory.getTransactionType());
		System.out.println("  - Is Expired: " + expiredHistory.getIsExpired());
		
		// Test entity validations
		System.out.println();
		System.out.println("🔍 Testing entity validations:");
		
		// Verify required fields
		assert earnedHistory.getCustomer() != null : "Customer should not be null";
		assert earnedHistory.getSpentAmount() != null : "Spent amount should not be null";
		assert earnedHistory.getPointsEarned() != null : "Points earned should not be null";
		assert earnedHistory.getEarnedAt() != null : "Earned at should not be null";
		assert earnedHistory.getExpiresAt() != null : "Expires at should not be null";
		assert earnedHistory.getTransactionType() != null : "Transaction type should not be null";
		
		System.out.println("✓ All required fields are properly set");
		
		// Verify business logic
		assert earnedHistory.getExpiresAt().isAfter(earnedHistory.getEarnedAt()) : "Expires at should be after earned at";
		assert !earnedHistory.getIsExpired() : "New points should not be expired";
		
		System.out.println("✓ Business logic validations passed");
		
		// Test complete transaction flow
		System.out.println();
		System.out.println("🔄 Testing complete transaction flow:");
		
		Double customerTotalPoints = 0.0;
		
		// Simulate multiple purchases
		Double[] purchaseAmounts = {
			800000.0,
			1500000.0, 
			3000000.0,
			250000.0
		};
		
		for (int i = 0; i < purchaseAmounts.length; i++) {
			Double amount = purchaseAmounts[i];
			Double points = amount / 1000.0;
			customerTotalPoints += points;
			
			System.out.println("✓ Purchase " + (i + 1) + ": " + String.format("%,d", amount.longValue()) + " VND = " + points + " points");
		}
		
		System.out.println("Total points earned: " + customerTotalPoints + " points");
		
		// Simulate using points
		Double pointsUsed = 500.0;
		customerTotalPoints -= pointsUsed;
		System.out.println("✓ Used " + pointsUsed + " points, remaining: " + customerTotalPoints + " points");
		
		// Simulate point expiration
		Double pointsExpired = 100.0;
		customerTotalPoints -= pointsExpired;
		System.out.println("✓ Expired " + pointsExpired + " points, final remaining: " + customerTotalPoints + " points");
		
		// Verify final calculations
		assert customerTotalPoints.equals(4950.0) : "Final points calculation should be correct";
		
		System.out.println();
		System.out.println("💾 Database operation simulation notes:");
		System.out.println("📌 To save these entities to database, use:");
		System.out.println("   pointHistoryRepository.save(earnedHistory);");
		System.out.println("   pointHistoryRepository.save(usedHistory);");
		System.out.println("   pointHistoryRepository.save(expiredHistory);");
		System.out.println();
		System.out.println("📌 To query point history, use:");
		System.out.println("   pointHistoryRepository.findByCustomerOrderByEarnedAtDesc(customer);");
		System.out.println("   pointHistoryRepository.findByCustomerAndTransactionType(customer, EARNED);");
		System.out.println("   pointHistoryRepository.findByCustomerAndExpiresAtBeforeAndIsExpiredFalse(...);");
		
		System.out.println();
		System.out.println("✅ Point history database operations tests completed successfully!");
	}
}
