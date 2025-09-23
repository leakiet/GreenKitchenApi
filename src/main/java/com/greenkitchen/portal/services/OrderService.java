package com.greenkitchen.portal.services;


import java.util.List;

import com.greenkitchen.portal.dtos.CreateOrderRequest;
import com.greenkitchen.portal.dtos.OrderResponse;
import com.greenkitchen.portal.dtos.PagedResponse;
import com.greenkitchen.portal.dtos.UpdateOrderRequest;
import com.greenkitchen.portal.entities.Order;


public interface OrderService {
    
    // Chỉ tạo 2 method như bạn yêu cầu
    Order createOrder(CreateOrderRequest request);

    PagedResponse<OrderResponse> listFilteredPaged(int page, int size, String status, String q, String fromDate, String toDate);

    List<Order> listAll();

    Order updateOrder(Long orderId, UpdateOrderRequest request);
    
    // Thêm method để lấy order theo ID cho PayPal integration
    Order getOrderById(Long orderId);

    OrderResponse getOrderByCode(String orderCode);

    // Method để complete COD payment khi delivery thành công
    Order completeCODOrder(Long orderId);
    
    // Method để update order status trong workflow
    Order updateOrderStatus(Long orderId, String newStatus);

    // Hủy đơn với lý do
    Order cancelOrder(Long orderId, String note);
}
