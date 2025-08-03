package com.greenkitchen.portal.enums;

public enum OrderStatus {
    PENDING,        // Đơn hàng đã tạo, chờ xác nhận
    CONFIRMED,      // Đã xác nhận đơn hàng
    PREPARING,      // Đang chuẩn bị món ăn
    READY,          // Sẵn sàng giao hàng
    SHIPPING,       // Đang giao hàng
    DELIVERED,      // Đã giao thành công
    CANCELLED       // Đã hủy
}
