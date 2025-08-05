package com.greenkitchen.portal.dtos;

import java.util.List;

import com.greenkitchen.portal.enums.CartStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
  private Long id;
  private Long customerId;
  private List<CartItemResponse> cartItems;
  private Double totalAmount;
  private CartStatus status;
  private Integer totalItems;
  private Integer totalQuantity;
}
