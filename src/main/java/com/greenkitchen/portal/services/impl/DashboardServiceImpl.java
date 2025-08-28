package com.greenkitchen.portal.services.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.DailyIncomeResponse;
import com.greenkitchen.portal.dtos.DailyTrendingMenuResponse;
import com.greenkitchen.portal.dtos.DashboardOverviewResponse;
import com.greenkitchen.portal.dtos.MostFavouriteItemResponse;
import com.greenkitchen.portal.dtos.OrderSuccessRateResponse;
import com.greenkitchen.portal.dtos.PopularFoodResponse;
import com.greenkitchen.portal.dtos.RecentOrderResponse;
import com.greenkitchen.portal.dtos.SalesFiguresResponse;
import com.greenkitchen.portal.repositories.DashboardRepository;
import com.greenkitchen.portal.services.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

  private final DashboardRepository dashboardRepository;

  @Autowired
  public DashboardServiceImpl(DashboardRepository dashboardRepository) {
    this.dashboardRepository = dashboardRepository;
  }

  @Override
  public List<MostFavouriteItemResponse> getMostFavouriteItems(Date from, Date to) {
    List<Object[]> results = dashboardRepository.getMostFavouriteItems(from, to);
    List<MostFavouriteItemResponse> responses = new ArrayList<>();
    for (Object[] row : results) {
      Long id = ((Number) row[0]).longValue();
      String title = (String) row[1];
      Double price = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
      Integer stock = row[3] != null ? ((Number) row[3]).intValue() : 0;
      String type = (String) row[4];
      Long count = ((Number) row[5]).longValue();
      String image = (String) row[6]; // Map image
      responses.add(new MostFavouriteItemResponse(id, title, price, stock, type, count, image));
    }
    return responses;
  }

  @Override
  public DashboardOverviewResponse getOverview(Date from, Date to) {
    // Dữ liệu hiện tại
    int totalOrders = dashboardRepository.countTotalOrders(from, to);
    int totalCustomers = dashboardRepository.countTotalCustomers(from, to);
    int totalMenus = dashboardRepository.countTotalMenus(from, to); // Thêm
    double totalIncome = dashboardRepository.sumTotalIncome(from, to);

    // Tính khoảng trước đó cùng độ dài
    long durationMillis = to.getTime() - from.getTime();
    Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    cal.add(Calendar.MILLISECOND, (int) -durationMillis);
    Date prevFrom = cal.getTime();
    Date prevTo = from;

    int prevOrders = dashboardRepository.countTotalOrders(prevFrom, prevTo);
    int prevCustomers = dashboardRepository.countTotalCustomers(prevFrom, prevTo);
    int prevMenus = dashboardRepository.countTotalMenus(prevFrom, prevTo); // Thêm
    double prevIncome = dashboardRepository.sumTotalIncome(prevFrom, prevTo);

    Double ordersChangePercent = prevOrders > 0
        ? Math.round(((double) (totalOrders - prevOrders) / prevOrders) * 100 * 100.0) / 100.0
        : null;
    Double customersChangePercent = prevCustomers > 0
        ? Math.round(((double) (totalCustomers - prevCustomers) / prevCustomers) * 100 * 100.0) / 100.0
        : null;
    Double menusChangePercent = prevMenus > 0 // Thêm
        ? Math.round(((double) (totalMenus - prevMenus) / prevMenus) * 100 * 100.0) / 100.0
        : null;
    Double incomeChangePercent = prevIncome > 0
        ? Math.round(((totalIncome - prevIncome) / prevIncome) * 100 * 100.0) / 100.0
        : null;

    return new DashboardOverviewResponse(totalOrders, totalCustomers, totalMenus, totalIncome, // Thêm totalMenus
        ordersChangePercent, customersChangePercent, menusChangePercent, incomeChangePercent); // Thêm
                                                                                               // menusChangePercent
  }

  @Override
  public OrderSuccessRateResponse getOrderSuccessRate(Date from, Date to) {
    int total = dashboardRepository.countTotalOrders(from, to);
    int success = dashboardRepository.countSuccessOrders(from, to);
    int cancelled = dashboardRepository.countCancelledOrders(from, to);
    double successRate = total > 0 ? ((double) success * 100.0 / total) : 0.0;
    double cancelledRate = total > 0 ? ((double) cancelled * 100.0 / total) : 0.0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    System.out.println("Debug: From=" + sdf.format(from) + ", To=" + sdf.format(to) + ", Total=" + total + ", Success="
        + success + ", SuccessRate=" + successRate); // Debug log
    return new OrderSuccessRateResponse(
        total,
        success,
        cancelled,
        successRate,
        cancelledRate,
        sdf.format(from),
        sdf.format(to));
  }

  @Override
  public PopularFoodResponse getPopularFoods(Date from, Date to) {
    List<Object[]> rows = dashboardRepository.getPopularFoods(from, to);
    List<PopularFoodResponse.PopularFoodItem> items = new ArrayList<>();

    int totalItems = rows.stream().mapToInt(row -> ((Number) row[1]).intValue()).sum();

    for (Object[] row : rows) {
      String type = (String) row[0];
      int count = ((Number) row[1]).intValue();
      double percentage = totalItems > 0 ? (count * 100.0 / totalItems) : 0.0;
      PopularFoodResponse.PopularFoodItem item = new PopularFoodResponse.PopularFoodItem();
      item.setType(type);
      item.setPercentage(percentage);
      items.add(item);
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    PopularFoodResponse dto = new PopularFoodResponse();
    dto.setItems(items);
    dto.setFromDate(sdf.format(from));
    dto.setToDate(sdf.format(to));
    return dto;
  }

  @Override
  public SalesFiguresResponse getSalesFigures(Date from, Date to, String type) {
    List<Object[]> results = dashboardRepository.getSalesFigures(from, to, type);
    List<String> labels = new ArrayList<>();
    List<Double> sales = new ArrayList<>();
    for (Object[] row : results) {
      labels.add((String) row[0]);
      sales.add(((Number) row[1]).doubleValue()); // COUNT(*) trả về Long, cast sang Double
    }
    return new SalesFiguresResponse(labels, sales);
  }

  @Override
  public DailyIncomeResponse getDailyIncome(Date date) {
    double income = dashboardRepository.getIncomeByDate(date);
    DailyIncomeResponse dto = new DailyIncomeResponse();
    dto.setCurrentIncome(income);
    dto.setPeriodLabel("Ngày");
    return dto;
  }

  @Override
  public List<RecentOrderResponse> getRecentOrders() {
    List<Object[]> results = dashboardRepository.getRecentOrders();
    List<RecentOrderResponse> responses = new ArrayList<>();
    for (Object[] row : results) {
      Long id = ((Number) row[0]).longValue();
      String customerName = String.valueOf(row[1]); // customer_id as String
      Double totalAmount = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
      String status = (String) row[3];
      Date createdAt = (Date) row[4];
      responses.add(new RecentOrderResponse(id, customerName, totalAmount, status, createdAt));
    }
    return responses;
  }

  @Override
  public List<DailyTrendingMenuResponse> getDailyTrendingMenus(Date date) {
    List<Object[]> results = dashboardRepository.getDailyTrendingMenus(date);
    List<DailyTrendingMenuResponse> responses = new ArrayList<>();
    for (Object[] row : results) {
      Long id = ((Number) row[0]).longValue();
      String title = (String) row[1];
      Double price = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
      Integer stock = row[3] != null ? ((Number) row[3]).intValue() : 0;
      String type = (String) row[4];
      Long count = ((Number) row[5]).longValue();
      String image = (String) row[6];
      String slug = (String) row[7]; // Map slug
      responses.add(new DailyTrendingMenuResponse(id, title, price, stock, type, count, image, slug));
    }
    return responses;
  }
}