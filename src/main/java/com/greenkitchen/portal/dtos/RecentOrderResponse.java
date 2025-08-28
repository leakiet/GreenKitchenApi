package com.greenkitchen.portal.dtos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RecentOrderResponse {
  private Long id;
  private String customerName;
  private Double totalAmount;
  private String status;
  private Date createdAt;
}
