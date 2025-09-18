	package com.greenkitchen.portal.dtos;
	
	import java.time.LocalDateTime;
	import java.util.List;

import com.greenkitchen.portal.enums.MessageStatus;

import lombok.AllArgsConstructor;
	import lombok.Getter;
	import lombok.NoArgsConstructor;
	import lombok.Setter;
	
	/**
	 * Phản hồi sau khi gửi hoặc nhận tin nhắn.
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public class ChatResponse {
	    private Long id;                  // id tin nhắn
	    private Long conversationId;      // id cuộc hội thoại
	    private String senderRole;        // "CUSTOMER", "EMP", "AI"
	    private String senderName;        // tên hiển thị người gửi hoặc AI
	    private String content;           // nội dung tin nhắn
	    private List<MenuMealLiteResponse> menu;
	    private LocalDateTime timestamp;  // thời điểm
	    private MessageStatus  status; // "PENDING", "SENT", "FAILED"
	    private String conversationStatus; // "AI", "WAITING_EMP", "EMP"

	    
	}
