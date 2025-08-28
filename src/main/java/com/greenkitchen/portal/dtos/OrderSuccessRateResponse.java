package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSuccessRateResponse {
    private int totalOrders;        // Tổng số đơn trong kỳ
    private int successOrders;      // Số đơn thành công
    private int cancelledOrders;    // Số đơn bị huỷ
    private double successRate;     // Tỷ lệ thành công (%)
    private double cancelledRate;   // Tỷ lệ huỷ (%)
    private String fromDate;        // Ngày bắt đầu kỳ lọc
    private String toDate;          // Ngày kết thúc kỳ lọc
}