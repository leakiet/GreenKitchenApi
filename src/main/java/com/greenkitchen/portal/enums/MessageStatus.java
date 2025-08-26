package com.greenkitchen.portal.enums;

public enum MessageStatus {
    PENDING,   // Đã tạo tin nhắn nhưng chưa có nội dung (AI chưa trả lời xong)
    SENT,      // Đã có nội dung hoàn chỉnh, hiển thị cho user
    FAILED     // Gọi AI lỗi, cần hiển thị xin lỗi
}
