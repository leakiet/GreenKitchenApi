package com.greenkitchen.portal.services;

import java.util.Date;
import java.util.List;

import com.greenkitchen.portal.dtos.DailyIncomeResponse;
import com.greenkitchen.portal.dtos.DailyTrendingMenuResponse;
import com.greenkitchen.portal.dtos.DashboardOverviewResponse;
import com.greenkitchen.portal.dtos.MostFavouriteItemResponse;
import com.greenkitchen.portal.dtos.OrderSuccessRateResponse;
import com.greenkitchen.portal.dtos.PopularFoodResponse;
import com.greenkitchen.portal.dtos.RecentOrderResponse;
import com.greenkitchen.portal.dtos.SalesFiguresResponse;

public interface DashboardService {
    DashboardOverviewResponse getOverview(Date fromDate, Date toDate);

    OrderSuccessRateResponse getOrderSuccessRate(Date fromDate, Date toDate);

    PopularFoodResponse getPopularFoods(Date fromDate, Date toDate);

    SalesFiguresResponse getSalesFigures(Date fromDate, Date toDate, String type);

    DailyIncomeResponse getDailyIncome(Date date);

    List<MostFavouriteItemResponse> getMostFavouriteItems(Date from, Date to);

    List<RecentOrderResponse> getRecentOrders();

    List<DailyTrendingMenuResponse> getDailyTrendingMenus(Date date);
}