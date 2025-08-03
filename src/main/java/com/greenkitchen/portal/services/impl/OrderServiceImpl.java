package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.dtos.CreateOrderRequest;
import com.greenkitchen.portal.dtos.UpdateOrderRequest;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomMeal;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.entities.Order;
import com.greenkitchen.portal.entities.OrderItem;
import com.greenkitchen.portal.enums.OrderStatus;
import com.greenkitchen.portal.enums.PaymentStatus;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.CustomMealRepository;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.repositories.OrderRepository;
import com.greenkitchen.portal.services.OrderService;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private MenuMealRepository menuMealRepository;

  @Autowired
  private CustomMealRepository customMealRepository;

  @Override
  public Order createOrder(CreateOrderRequest request) {
    // Validate required fields
    if (request.getCustomerId() == null) {
      throw new RuntimeException("Customer ID is required");
    }

    if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
      throw new RuntimeException("Order items are required");
    }

    if (request.getSubtotal() == null) {
      throw new RuntimeException("Subtotal is required");
    }

    if (request.getShippingFee() == null) {
      throw new RuntimeException("Shipping fee is required");
    }

    // Tìm customer
    Customer customer = customerRepository.findById(request.getCustomerId())
        .orElseThrow(() -> new RuntimeException("Customer not found"));

    // Tạo Order entity manually thay vì dùng ModelMapper
    Order order = new Order();
    order.setCustomer(customer);

    // Map thông tin giao hàng
    order.setStreet(request.getStreet());
    order.setWard(request.getWard());
    order.setDistrict(request.getDistrict());
    order.setCity(request.getCity());
    order.setRecipientName(request.getRecipientName());
    order.setRecipientPhone(request.getRecipientPhone());
    order.setDeliveryTime(request.getDeliveryTime());

    // Map thông tin giá cả
    order.setSubtotal(request.getSubtotal());
    order.setShippingFee(request.getShippingFee());
    order.setMembershipDiscount(request.getMembershipDiscount() != null ? request.getMembershipDiscount() : 0.0);
    order.setCouponDiscount(request.getCouponDiscount() != null ? request.getCouponDiscount() : 0.0);
    order.setNotes(request.getNotes());
    order.setPaymentMethod(request.getPaymentMethod());

    // Set status based on payment method
    if ("COD".equals(request.getPaymentMethod())) {
      order.setStatus(OrderStatus.PENDING);
      order.setPaymentStatus(PaymentStatus.PENDING);
    } else if ("CARD".equals(request.getPaymentMethod())) {
      order.setStatus(OrderStatus.PENDING); // Sẽ thay đổi sau khi có payment gateway
      order.setPaymentStatus(PaymentStatus.PENDING);
    }

    // Calculate and set point earn (1% of total amount)
    Double pointEarn = Math.round(request.getTotalAmount() * 0.01 * 100.0) / 100.0;
    order.setPointEarn(pointEarn);

    // Tạo OrderItems manually
    List<OrderItem> orderItems = request.getOrderItems().stream()
        .map(itemRequest -> {
          OrderItem orderItem = new OrderItem();
          orderItem.setOrder(order);
          orderItem.setItemType(itemRequest.getItemType());
          orderItem.setQuantity(itemRequest.getQuantity());
          orderItem.setUnitPrice(itemRequest.getUnitPrice());
          orderItem.setNotes(itemRequest.getNotes());

          // Set product references
          switch (itemRequest.getItemType()) {
            case MENU_MEAL:
              MenuMeal menuMeal = menuMealRepository.findById(itemRequest.getMenuMealId())
                  .orElseThrow(() -> new RuntimeException("MenuMeal not found"));
              orderItem.setMenuMeal(menuMeal);
              orderItem.setTitle(menuMeal.getTitle());
              orderItem.setDescription(menuMeal.getDescription());
              orderItem.setImage(menuMeal.getImage());
              break;
            case CUSTOM_MEAL:
              CustomMeal customMeal = customMealRepository.findById(itemRequest.getCustomMealId())
                  .orElseThrow(() -> new RuntimeException("CustomMeal not found"));
              orderItem.setCustomMeal(customMeal);
              // orderItem.setTitle(customMeal.getTitle());
              // orderItem.setDescription(customMeal.getDescription());
              // orderItem.setImage(customMeal.getImage());
              break;
          }

          // Calculate total price
          orderItem.setTotalPrice(orderItem.getUnitPrice() * orderItem.getQuantity());
          return orderItem;
        })
        .collect(Collectors.toList());

    order.setOrderItems(orderItems);

    // Set total amount from request (frontend calculated)
    if (request.getTotalAmount() != null) {
      order.setTotalAmount(request.getTotalAmount());
    } else {
      // Fallback calculation if totalAmount not provided
      double membershipDiscount = request.getMembershipDiscount() != null ? request.getMembershipDiscount() : 0.0;
      double couponDiscount = request.getCouponDiscount() != null ? request.getCouponDiscount() : 0.0;
      double totalAmount = request.getSubtotal() + request.getShippingFee() - membershipDiscount - couponDiscount;
      order.setTotalAmount(Math.max(0, totalAmount));
    }

    return orderRepository.save(order);
  }

  @Override
  public Order updateOrder(Long orderId, UpdateOrderRequest request) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    // Update fields manually
    if (request.getStatus() != null) {
      order.setStatus(request.getStatus());
    }
    if (request.getPaymentStatus() != null) {
      order.setPaymentStatus(request.getPaymentStatus());
    }
    if (request.getDeliveryTime() != null) {
      order.setDeliveryTime(request.getDeliveryTime());
    }
    if (request.getNotes() != null) {
      order.setNotes(request.getNotes());
    }

    // Update delivery address fields
    if (request.getStreet() != null) {
      order.setStreet(request.getStreet());
    }
    if (request.getWard() != null) {
      order.setWard(request.getWard());
    }
    if (request.getDistrict() != null) {
      order.setDistrict(request.getDistrict());
    }
    if (request.getCity() != null) {
      order.setCity(request.getCity());
    }
    if (request.getRecipientName() != null) {
      order.setRecipientName(request.getRecipientName());
    }
    if (request.getRecipientPhone() != null) {
      order.setRecipientPhone(request.getRecipientPhone());
    }

    return orderRepository.save(order);
  }
}
