package com.greenkitchen.portal.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

//ConversationResponse.java (DTO)
@Data
@NoArgsConstructor
public class ConversationResquest {
 private Long conversationId; // ID của cuộc hội thoại
 private Long employeeId; // ID của nhân viên (null nếu chưa có nhân viên tham gia)
 private String customerName; // hoặc ẩn danh nếu guest
 private String status;
 private String lastMessage;
 private int unreadCount;
}
