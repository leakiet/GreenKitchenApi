package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Yêu cầu gửi tin nhắn trong conversation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private Long conversationId;      // id cuộc hội thoại (tạo mới nếu guest lần đầu)
    private String senderRole;        // "CUSTOMER", "EMP", "AI"
    private String content;           // nội dung tin nhắn
    private String lang;              // ngôn ngữ ("vi", "en", ...)
    private String idempotencyKey;    // khóa chống gửi trùng
}
	