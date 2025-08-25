package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.CreateOrderRequest;
import com.greenkitchen.portal.dtos.UpdateOrderRequest;
import com.greenkitchen.portal.entities.Order;
import com.greenkitchen.portal.enums.OrderStatus;
import com.greenkitchen.portal.enums.PaymentStatus;
import com.greenkitchen.portal.services.OrderService;
import com.greenkitchen.portal.services.impl.MembershipServiceImpl;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/apis/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MembershipServiceImpl membershipService;

    // Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        //Tao order
        Order order = orderService.createOrder(request);

        // Update Customer Point
        if (order.getPaymentStatus().equals(PaymentStatus.COMPLETED)) {
            membershipService.updateMembershipAfterPurchase(request.getCustomerId(), order.getTotalAmount(), order.getPointEarn(), order.getId());
        }

        return ResponseEntity.ok(order);
    }

    // Lấy thông tin đơn hàng theo ID
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable("orderId") Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search/{orderCode}")
    public ResponseEntity<?> getOrderByCode(@PathVariable("orderCode") String orderCode) {
        try {
            Order order = orderService.getOrderByCode(orderCode);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }
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
    
    // Complete COD payment khi delivery thành công
    @PostMapping("/{orderId}/complete-cod")
    public ResponseEntity<?> completeCODPayment(@PathVariable Long orderId) {
        try {
            Order order = orderService.completeCODOrder(orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    // Update order status trong workflow
    @PutMapping("/{orderId}/status/{newStatus}")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId, 
            @PathVariable String newStatus) {
        try {
            Order order = orderService.updateOrderStatus(orderId, newStatus);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
