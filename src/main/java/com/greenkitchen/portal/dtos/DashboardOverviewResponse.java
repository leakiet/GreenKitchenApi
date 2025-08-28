package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DashboardOverviewResponse {
  private int totalOrders;
  private int totalCustomers;
  private int totalMenus;
  private double totalIncome;
  private Double ordersChangePercent;
  private Double customersChangePercent;
  private Double menusChangePercent;
  private Double incomeChangePercent;
}
