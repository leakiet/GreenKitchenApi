package com.greenkitchen.portal.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.DailyIncomeResponse;
import com.greenkitchen.portal.dtos.DailyTrendingMenuResponse;
import com.greenkitchen.portal.dtos.DashboardOverviewResponse;
import com.greenkitchen.portal.dtos.MostFavouriteItemResponse;
import com.greenkitchen.portal.dtos.OrderSuccessRateResponse;
import com.greenkitchen.portal.dtos.PopularFoodResponse;
import com.greenkitchen.portal.dtos.RecentOrderResponse;
import com.greenkitchen.portal.dtos.SalesFiguresResponse;
import com.greenkitchen.portal.services.DashboardService;

@RestController
@RequestMapping("/apis/v1/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  @Autowired
  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/overview")
  public ResponseEntity<DashboardOverviewResponse> getOverview(
      @RequestParam(name = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
      @RequestParam(name = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to) {
    DashboardOverviewResponse response = dashboardService.getOverview(from, to);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping("/order-success-rate")
  public ResponseEntity<OrderSuccessRateResponse> getOrderSuccessRate(
      @RequestParam(name = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
      @RequestParam(name = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to) {
    OrderSuccessRateResponse response = dashboardService.getOrderSuccessRate(from, to);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping("/most-favourite-items")
  public ResponseEntity<List<MostFavouriteItemResponse>> getMostFavouriteItems(
      @RequestParam(name = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
      @RequestParam(name = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to) {
    List<MostFavouriteItemResponse> response = dashboardService.getMostFavouriteItems(from, to);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/popular-foods")
  public ResponseEntity<PopularFoodResponse> getPopularFoods(
      @RequestParam(name = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
      @RequestParam(name = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to) {
    PopularFoodResponse response = dashboardService.getPopularFoods(from, to);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping("/sales-figures")
  public ResponseEntity<SalesFiguresResponse> getSalesFigures(
      @RequestParam(name = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
      @RequestParam(name = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
      @RequestParam(name = "type", defaultValue = "day") String type) {
    SalesFiguresResponse response = dashboardService.getSalesFigures(from, to, type);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping("/daily-income")
  public ResponseEntity<DailyIncomeResponse> getDailyIncome(
      @RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
    DailyIncomeResponse response = dashboardService.getDailyIncome(date);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping("/recent-orders")
  public ResponseEntity<List<RecentOrderResponse>> getRecentOrders() {
    List<RecentOrderResponse> response = dashboardService.getRecentOrders();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/weekly-trending-menus") // Đổi tên endpoint nếu muốn
  public ResponseEntity<List<DailyTrendingMenuResponse>> getWeeklyTrendingMenus(
      @RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
    List<DailyTrendingMenuResponse> response = dashboardService.getDailyTrendingMenus(date); // Giữ method name hoặc đổi
    return ResponseEntity.ok(response);
  }
}