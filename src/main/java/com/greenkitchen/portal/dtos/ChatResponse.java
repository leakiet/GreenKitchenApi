package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;

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
    private LocalDateTime timestamp;  // thời điểm
    
}
