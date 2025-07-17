package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

//ConversationResponse.java (DTO)
@Data
@AllArgsConstructor
public class ConversationResquest {
 private Long id;
 private String customerName; // hoặc ẩn danh nếu guest
 private String status;
 private String lastMessage;
 private int unreadCount;
}
