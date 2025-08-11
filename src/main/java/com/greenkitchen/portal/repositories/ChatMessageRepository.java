package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.ChatMessage;
import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.enums.SenderType;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	List<ChatMessage> findByConversation(Conversation conv);

	// Tùy chọn: lấy nhanh theo conversationId
	List<ChatMessage> findByConversationId(Long conversationId);

	// Hàm phân trang: lấy theo conversationId, sắp xếp theo timestamp DESC (tin
	// nhắn mới nhất trước)
	Page<ChatMessage> findByConversationIdOrderByTimestampDesc(Long conversationId, Pageable pageable);

	// Hàm đếm số tin nhắn chưa đọc theo conversation và loại người gửi
	int countByConversationAndSenderTypeAndIsReadFalse(Conversation conversation, SenderType senderType);

	// (optionally) lấy list chưa đọc để đánh dấu đã đọc
	List<ChatMessage> findByConversationAndSenderTypeAndIsReadFalse(Conversation conversation, SenderType senderType);

	@Modifying
	@Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversation = :conv AND m.senderType = :senderType AND m.isRead = false")
	void markMessagesAsRead(@Param("conv") Conversation conv, @Param("senderType") SenderType senderType);

	// Lấy 10 tin nhắn mới nhất theo conversation, sắp xếp theo timestamp DESC
	List<ChatMessage> findTop20ByConversationOrderByTimestampDesc(Conversation conv);

}
