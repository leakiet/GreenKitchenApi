package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConversationResponse {
	private Long conversationId;
	private String customerName; // hoặc ẩn danh nếu guest
	
	private String status;
	private String lastMessage;
	private String lastMessageTime;
	private int unreadCount;
    private Long employeeId;
}
