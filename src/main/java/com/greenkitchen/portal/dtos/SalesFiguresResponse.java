package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SalesFiguresResponse {
  private List<String> labels; // Tên tháng/ngày (Jan, Feb, ...)
  private List<Double> sales;
}
