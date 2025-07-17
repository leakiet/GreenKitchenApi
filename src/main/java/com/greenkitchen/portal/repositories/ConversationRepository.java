package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.Employee;
import com.greenkitchen.portal.entities.ConversationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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

}
