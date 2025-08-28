package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import com.greenkitchen.portal.dtos.CreateOrderRequest;
import com.greenkitchen.portal.dtos.OrderResponse;
import com.greenkitchen.portal.dtos.PagedResponse;
import com.greenkitchen.portal.dtos.UpdateOrderRequest;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomMeal;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.entities.Order;
import com.greenkitchen.portal.entities.OrderItem;
import com.greenkitchen.portal.entities.Payment;
import com.greenkitchen.portal.enums.OrderStatus;
import com.greenkitchen.portal.enums.PaymentMethod;
import com.greenkitchen.portal.enums.PaymentStatus;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.CustomMealRepository;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.repositories.OrderRepository;
import com.greenkitchen.portal.repositories.PaymentRepository;
import com.greenkitchen.portal.services.OrderService;
import com.greenkitchen.portal.services.PaymentService;



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

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private MembershipServiceImpl membershipService;

  @Override
  public List<Order> listAll() {
    return orderRepository.findAll().stream()
        .filter(o -> o.getIsDeleted() == null || !o.getIsDeleted())
        .collect(Collectors.toList());
  }

  @Override
  public Order createOrder(CreateOrderRequest request) {
    // Tìm customer
    Customer customer = customerRepository.findById(request.getCustomerId())
        .orElseThrow(() -> new RuntimeException("Customer not found"));

    Order order = new Order();
    order.setCustomer(customer);

    String orderCode = "GK-" + System.currentTimeMillis();
    order.setOrderCode(orderCode);

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

    order.setPaypalOrderId(request.getPaypalOrderId());

    // Set status based on payment method
    if ("COD".equals(request.getPaymentMethod())) {
      order.setStatus(OrderStatus.PENDING);
      order.setPaymentStatus(PaymentStatus.PENDING);
    } else if ("PAYPAL".equals(request.getPaymentMethod())) {
      order.setStatus(OrderStatus.CONFIRMED);
      order.setConfirmedAt(LocalDateTime.now());
      order.setPaymentStatus(PaymentStatus.COMPLETED);
    }

    // Calculate and set point earn (1000 VND = 1 point)
    Double pointEarn = Math.round(request.getTotalAmount() * 0.001 * 100.0) / 100.0;
    order.setPointEarn(pointEarn);

    // Tạo OrderItems
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
              orderItem.setTitle(customMeal.getTitle());
              orderItem.setDescription(customMeal.getDescription());
              orderItem.setImage(customMeal.getImage());
            case WEEK_MEAL:
              // WeekMeal weekMeal = weekMealRepository.findById(itemRequest.getWeekMealId())
              // .orElseThrow(() -> new RuntimeException("WeekMeal not found"));
              // orderItem.setWeekMeal(weekMeal);
              // orderItem.setTitle(weekMeal.getTitle());
              // orderItem.setDescription(weekMeal.getDescription());
              // orderItem.setImage(weekMeal.getImage());
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

    Order savedOrder = orderRepository.save(order);

    // Tạo Payment record dựa trên payment method
    if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
      // Tạo COD payment với status PENDING - chỉ complete khi delivery thành công
      paymentService.createCODPayment(savedOrder, customer, savedOrder.getTotalAmount(),
          "COD payment for order #" + savedOrder.getId());
    } else if ("PAYPAL".equalsIgnoreCase(request.getPaymentMethod())) {
      // Tạo PayPal payment và complete luôn
      paymentService.createPayPalPayment(savedOrder, customer, savedOrder.getTotalAmount(),
          "paypal_order_" + savedOrder.getId());
    }

    return savedOrder;
  }

  @Override
  public PagedResponse<OrderResponse> listFilteredPaged(int page, int size, String status, String q, String fromDate,
      String toDate) {
    Pageable pg = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
    OrderStatus ot = null;
    if (status != null && !status.isEmpty()) {
      try {
        ot = OrderStatus.valueOf(status);
      } catch (IllegalArgumentException ex) {
        ot = null;
      }
    }
    LocalDateTime from = null;
    LocalDateTime to = null;
    try {
      if (fromDate != null && !fromDate.isEmpty()) {
        from = java.time.LocalDate.parse(fromDate).atStartOfDay();
      }
      if (toDate != null && !toDate.isEmpty()) {
        to = java.time.LocalDate.parse(toDate).atTime(23, 59, 59);
      }
    } catch (Exception e) {
      // ignore parse error, fallback null
    }
    Page<Order> p = orderRepository.findFilteredPaged(ot, q == null || q.isEmpty() ? null : q, from, to, pg);
    PagedResponse<OrderResponse> res = new PagedResponse<>();
    res.setItems(p.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
    res.setTotal(p.getTotalElements());
    res.setPage(page);
    res.setSize(size);
    return res;
  }

  private OrderResponse toResponse(Order order) {
  ModelMapper modelMapper = new ModelMapper();
  OrderResponse dto = modelMapper.map(order, OrderResponse.class);
  // Add customerId manually
  dto.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
  return dto;
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

  @Override
  public Order getOrderById(Long orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
  }

  @Override
  public OrderResponse getOrderByCode(String orderCode) {
    try {
      Order order = orderRepository.findByOrderCode(orderCode);
      if (order == null) {
        throw new IllegalArgumentException("Order not found with code: " + orderCode);
      }
      return toResponse(order);
    } catch (Exception e) {
      throw new RuntimeException("Order not found with code: " + orderCode);
    }
  }

  @Override
  public Order completeCODOrder(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

    // Chỉ complete nếu là COD order và đang pending payment
    if (!"COD".equalsIgnoreCase(order.getPaymentMethod())) {
      throw new RuntimeException("Order is not COD payment method");
    }

    if (order.getPaymentStatus() != PaymentStatus.PENDING) {
      throw new RuntimeException("Order payment is not in PENDING status");
    }

    // Chỉ complete COD khi order đang SHIPPING (nhân viên đang giao hàng)
    if (order.getStatus() != OrderStatus.SHIPPING) {
      throw new RuntimeException(
          "Order must be in SHIPPING status to complete COD payment. Current status: " + order.getStatus());
    }

    // Tìm COD payment của order này
    Payment codPayment = paymentRepository.findByOrderIdAndPaymentMethod(orderId, PaymentMethod.COD)
        .orElseThrow(() -> new RuntimeException("COD Payment not found for order: " + orderId));

    // Complete COD payment
    paymentService.completeCODPayment(codPayment.getId());

    // Update order status to DELIVERED và payment status
    order.setPaymentStatus(PaymentStatus.COMPLETED);
    order.setStatus(OrderStatus.DELIVERED);

    Order savedOrder = orderRepository.save(order);

    // Gọi updateMembershipAfterPurchase để cộng điểm khi COD shipping thành công
    try {
      membershipService.updateMembershipAfterPurchase(
          order.getCustomer().getId(),
          order.getTotalAmount(),
          order.getPointEarn(),
          order.getId());
      System.out.println("✅ COD order completed - loyalty points updated for customer: " + order.getCustomer().getId());
    } catch (Exception e) {
      System.err.println("❌ Error updating membership after COD completion: " + e.getMessage());
      // Don't fail the order completion if membership update fails
    }

    return savedOrder;
  }

  //UPDATE ORDER STATUS !!!!!!
  @Override
  public Order updateOrderStatus(Long orderId, String newStatus) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

    // Validate status transition
    OrderStatus currentStatus = order.getStatus();
    OrderStatus targetStatus;

    try {
      targetStatus = OrderStatus.valueOf(newStatus.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid order status: " + newStatus);
    }

    // Validate workflow cho COD orders
    if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
      validateCODStatusTransition(currentStatus, targetStatus);
    }

    // Update status
    order.setStatus(targetStatus);

    if(targetStatus == OrderStatus.CANCELLED) {
      order.setCanceledAt(LocalDateTime.now());
    }
    if(targetStatus == OrderStatus.CONFIRMED) {
      order.setConfirmedAt(LocalDateTime.now());
    }
    if(targetStatus == OrderStatus.PREPARING) {
      order.setPreparingAt(LocalDateTime.now());
    }
    if(targetStatus == OrderStatus.SHIPPING) {
      order.setShippingAt(LocalDateTime.now());
    }
    if(targetStatus == OrderStatus.DELIVERED) {
      order.setDeliveredAt(LocalDateTime.now());
    }

    // Special handling cho DELIVERED status - Update membership, complete payment
    if (targetStatus == OrderStatus.DELIVERED && "COD".equalsIgnoreCase(order.getPaymentMethod())) {
      // Nếu update trực tiếp sang DELIVERED, tự động complete COD payment
      if (order.getPaymentStatus() == PaymentStatus.PENDING) {
        Payment codPayment = paymentRepository.findByOrderIdAndPaymentMethod(orderId, PaymentMethod.COD)
            .orElseThrow(() -> new RuntimeException("COD Payment not found for order: " + orderId));

        paymentService.completeCODPayment(codPayment.getId());
        order.setPaymentStatus(PaymentStatus.COMPLETED);

        // Gọi updateMembershipAfterPurchase khi COD order DELIVERED
        try {
          membershipService.updateMembershipAfterPurchase(
              order.getCustomer().getId(),
              order.getTotalAmount(),
              order.getPointEarn(),
              order.getId());
        } catch (Exception e) {
          System.err.println("❌ Error updating membership after COD delivery: " + e.getMessage());
          // Don't fail the status update if membership update fails
        }
      }
    }

    Order savedOrder = orderRepository.save(order);
    return savedOrder;
  }

  /**
   * Validate COD order status transitions
   */
  private void validateCODStatusTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
    switch (currentStatus) {
      case PENDING:
        if (targetStatus != OrderStatus.CONFIRMED && targetStatus != OrderStatus.CANCELLED) {
          throw new RuntimeException("From PENDING, can only move to CONFIRMED or CANCELLED");
        }
        break;
      case CONFIRMED:
        if (targetStatus != OrderStatus.PREPARING && targetStatus != OrderStatus.CANCELLED) {
          throw new RuntimeException("From CONFIRMED, can only move to PREPARING or CANCELLED");
        }
        break;
      case PREPARING:
        if (targetStatus != OrderStatus.SHIPPING && targetStatus != OrderStatus.CANCELLED) {
          throw new RuntimeException("From PREPARING, can only move to SHIPPING or CANCELLED");
        }
        break;
      case SHIPPING:
        if (targetStatus != OrderStatus.DELIVERED && targetStatus != OrderStatus.CANCELLED) {
          throw new RuntimeException("From SHIPPING, can only move to DELIVERED or CANCELLED");
        }
        break;
      case DELIVERED:
        throw new RuntimeException("Cannot change status from DELIVERED");
      case CANCELLED:
        throw new RuntimeException("Cannot change status from CANCELLED");
    }
  }
}
