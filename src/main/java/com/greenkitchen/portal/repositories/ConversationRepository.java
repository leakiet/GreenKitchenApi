package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.Employee;
import com.greenkitchen.portal.enums.ConversationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
	// Giữ lại
	List<Conversation> findByCustomer(Customer customer);

	// Sửa trả về Conversation để dễ truy vấn
	List<Conversation> findByEmployee(Employee employee);

	// Mới: truy vấn theo trạng thái
	List<Conversation> findByStatus(ConversationStatus status);

	// Mới: chỉ lấy conversation đang chat với emp
	List<Conversation> findByEmployeeAndStatus(Employee employee, ConversationStatus status);

	// Mới: lấy tất cả conversation theo danh sách trạng thái
	List<Conversation> findByStatusIn(List<ConversationStatus> statuses);

	// Cho EMP (hoặc dùng cho mọi đối tượng)
	List<Conversation> findByStatusInOrderByUpdatedAtDesc(List<ConversationStatus> statuses);

	// Nếu lấy tất cả:
	List<Conversation> findAllByOrderByUpdatedAtDesc();

	// Lấy theo EMP và danh sách trạng thái, sắp xếp theo thời gian cập nhật mới
	// nhất
	List<Conversation> findByEmployeeAndStatusInOrderByUpdatedAtDesc(Employee emp, List<ConversationStatus> statuses);

	List<Conversation> findByStatusInAndCreatedAtBetweenOrderByCreatedAtDesc(List<ConversationStatus> statuses, LocalDateTime fromDate, LocalDateTime toDate);

	// FIX: Optimized query với JOIN FETCH để tránh N+1 problem
	@Query("SELECT DISTINCT c FROM Conversation c " +
		   "LEFT JOIN FETCH c.customer " +
		   "LEFT JOIN FETCH c.employee " +
		   "WHERE c.status IN :statuses " +
		   "ORDER BY c.updatedAt DESC")
	List<Conversation> findByStatusInWithJoinsOrderByUpdatedAtDesc(@Param("statuses") List<ConversationStatus> statuses);

	// FIX: Optimized query với date filter và JOIN FETCH
	@Query("SELECT DISTINCT c FROM Conversation c " +
		   "LEFT JOIN FETCH c.customer " +
		   "LEFT JOIN FETCH c.employee " +
		   "WHERE c.status IN :statuses " +
		   "AND c.createdAt BETWEEN :fromDate AND :toDate " +
		   "ORDER BY c.createdAt DESC")
	List<Conversation> findByStatusInWithJoinsAndDateFilterOrderByCreatedAtDesc(
		@Param("statuses") List<ConversationStatus> statuses,
		@Param("fromDate") LocalDateTime fromDate,
		@Param("toDate") LocalDateTime toDate);

		// FIX: Query tối ưu để lấy conversation với last message và unread count
	@Query("SELECT c.id, c.status, c.updatedAt, " +
		   "COALESCE(c.customer.firstName, 'Khách vãng lai') as customerName, " +
		   "c.customer.phone as customerPhone, " +
		   "c.employee.id as employeeId, " +
		   "COALESCE(lastMsg.content, '') as lastMessage, " +
		   "COALESCE(lastMsg.timestamp, c.createdAt) as lastMessageTime, " +
		   "COALESCE(unreadCount.count, 0) as unreadCount " +
		   "FROM Conversation c " +
		   "LEFT JOIN c.customer " +
		   "LEFT JOIN c.employee " +
		   "LEFT JOIN ChatMessage lastMsg ON lastMsg.conversation.id = c.id " +
		   "AND lastMsg.timestamp = (SELECT MAX(m.timestamp) FROM ChatMessage m WHERE m.conversation.id = c.id) " +
		   "LEFT JOIN (SELECT cm.conversation.id as convId, COUNT(cm) as count " +
		   "FROM ChatMessage cm WHERE cm.senderType = 'CUSTOMER' AND cm.isRead = false " +
		   "GROUP BY cm.conversation.id) unreadCount ON unreadCount.convId = c.id " +
		   "WHERE c.status IN :statuses " +
		   "ORDER BY c.updatedAt DESC")
	List<Object[]> findConversationsWithLastMessageAndUnreadCount(@Param("statuses") List<ConversationStatus> statuses);

	// FIX: Query tối ưu với date filter
	@Query("SELECT c.id, c.status, c.updatedAt, " +
		   "COALESCE(c.customer.firstName, 'Khách vãng lai') as customerName, " +
		   "c.customer.phone as customerPhone, " +
		   "c.employee.id as employeeId, " +
		   "COALESCE(lastMsg.content, '') as lastMessage, " +
		   "COALESCE(lastMsg.timestamp, c.createdAt) as lastMessageTime, " +
		   "COALESCE(unreadCount.count, 0) as unreadCount " +
		   "FROM Conversation c " +
		   "LEFT JOIN c.customer " +
		   "LEFT JOIN c.employee " +
		   "LEFT JOIN ChatMessage lastMsg ON lastMsg.conversation.id = c.id " +
		   "AND lastMsg.timestamp = (SELECT MAX(m.timestamp) FROM ChatMessage m WHERE m.conversation.id = c.id) " +
		   "LEFT JOIN (SELECT cm.conversation.id as convId, COUNT(cm) as count " +
		   "FROM ChatMessage cm WHERE cm.senderType = 'CUSTOMER' AND cm.isRead = false " +
		   "GROUP BY cm.conversation.id) unreadCount ON unreadCount.convId = c.id " +
		   "WHERE c.status IN :statuses " +
		   "AND c.createdAt BETWEEN :fromDate AND :toDate " +
		   "ORDER BY c.createdAt DESC")
	List<Object[]> findConversationsWithLastMessageAndUnreadCountAndDateFilter(
		@Param("statuses") List<ConversationStatus> statuses,
		@Param("fromDate") LocalDateTime fromDate,
		@Param("toDate") LocalDateTime toDate);

}
