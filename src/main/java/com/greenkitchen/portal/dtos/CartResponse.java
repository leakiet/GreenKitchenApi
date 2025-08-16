package com.greenkitchen.portal.dtos;

import java.util.List;

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
  private Integer totalItems;
  private Integer totalQuantity;
}
