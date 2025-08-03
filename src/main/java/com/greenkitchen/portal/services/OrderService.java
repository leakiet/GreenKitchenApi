package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.CreateOrderRequest;
import com.greenkitchen.portal.dtos.UpdateOrderRequest;
import com.greenkitchen.portal.entities.Order;

public interface OrderService {
    
    // Chỉ tạo 2 method như bạn yêu cầu
    Order createOrder(CreateOrderRequest request);
    
    Order updateOrder(Long orderId, UpdateOrderRequest request);
}
