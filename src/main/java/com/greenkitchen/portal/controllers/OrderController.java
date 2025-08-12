package com.greenkitchen.portal.controllers;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.CreateOrderRequest;
import com.greenkitchen.portal.dtos.UpdateOrderRequest;
import com.greenkitchen.portal.entities.Order;
import com.greenkitchen.portal.services.OrderService;
import com.greenkitchen.portal.services.impl.MembershipServiceImpl;

@RestController
@RequestMapping("/apis/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MembershipServiceImpl membershipService;

    // Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        //Tao order
        Order order = orderService.createOrder(request);

        //Update Customer Point
        membershipService.updateMembershipAfterPurchase(request.getCustomerId(), order.getTotalAmount(), order.getPointEarn(), order.getId());

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    // Cập nhật đơn hàng
    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderRequest request) {
        try {
            Order order = orderService.updateOrder(orderId, request);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
