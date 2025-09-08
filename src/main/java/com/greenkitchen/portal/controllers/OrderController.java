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
import com.greenkitchen.portal.dtos.OrderResponse;
import com.greenkitchen.portal.dtos.UpdateOrderStatusRequest;
import com.greenkitchen.portal.entities.Order;
import com.greenkitchen.portal.enums.PaymentStatus;
import com.greenkitchen.portal.services.OrderService;
import com.greenkitchen.portal.services.impl.MembershipServiceImpl;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/apis/v1/orders")
public class OrderController {
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MembershipServiceImpl membershipService;

    // Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        //Tao order
        Order order = orderService.createOrder(request);

        // Update Customer Point if payment is completed
        if (order.getPaymentStatus().equals(PaymentStatus.COMPLETED)) {
            membershipService.updateMembershipAfterPurchase(
                request.getCustomerId(),
                order.getTotalAmount(),
                order.getPointEarn(),
                order.getId()
            );
        }

        // Gửi notification tới staff qua WebSocket
        messagingTemplate.convertAndSend("/topic/order/new", order);

        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/filter")
    public ResponseEntity<?> listFilteredOrders(@RequestParam(value = "page", required = false) Integer page,
                                                @RequestParam(value = "size", required = false) Integer size,
                                                @RequestParam(value = "status", required = false) String status,
                                                @RequestParam(value = "q", required = false) String q,
                                                @RequestParam(value = "fromDate", required = false) String fromDate,
                                                @RequestParam(value = "toDate", required = false) String toDate) {
            if (page != null && size != null) {
                var res = orderService.listFilteredPaged(page, size, status, q, fromDate, toDate);
                return ResponseEntity.ok(res);
            }
            var list = orderService.listAll();
            return ResponseEntity.ok(list);
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
            OrderResponse order = orderService.getOrderByCode(orderCode);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
    }

    //Update Status
    @PutMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(@RequestBody UpdateOrderStatusRequest request){
        orderService.updateOrderStatus(request.getId(), request.getStatus());
        return ResponseEntity.ok("Update successful");
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
    
}
