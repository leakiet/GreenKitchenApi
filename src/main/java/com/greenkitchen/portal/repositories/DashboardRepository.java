package com.greenkitchen.portal.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.Order;

public interface DashboardRepository extends JpaRepository<Order, Long> {

  @Query(value = "SELECT mm.id, mm.title, mm.price, mm.stock, mm.type, COUNT(oi.id) AS cnt, mm.image FROM order_items oi JOIN orders o ON o.id = oi.order_id JOIN menu_meals mm ON oi.menu_meal_id = mm.id WHERE DATE(o.created_at) BETWEEN DATE(:from) AND DATE(:to) GROUP BY mm.id, mm.title, mm.price, mm.stock, mm.type, mm.image ORDER BY cnt DESC LIMIT 5", nativeQuery = true)
  List<Object[]> getMostFavouriteItems(@Param("from") Date fromDate, @Param("to") Date toDate);

  @Query(value = "SELECT COUNT(*) FROM orders WHERE DATE(created_at) BETWEEN DATE(:from) AND DATE(:to)", nativeQuery = true)
  int countTotalOrders(@Param("from") Date fromDate, @Param("to") Date toDate);

  @Query(value = "SELECT COUNT(*) FROM menu_meals WHERE DATE(created_at) BETWEEN DATE(:from) AND DATE(:to)", nativeQuery = true)
  int countTotalMenus(@Param("from") Date fromDate, @Param("to") Date toDate);

  @Query(value = "SELECT COUNT(*) FROM customers WHERE DATE(created_at) BETWEEN DATE(:from) AND DATE(:to)", nativeQuery = true)
  int countTotalCustomers(@Param("from") Date fromDate, @Param("to") Date toDate);

  @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE payment_status = 'COMPLETED' AND DATE(created_at) BETWEEN DATE(:from) AND DATE(:to)", nativeQuery = true)
  double sumTotalIncome(@Param("from") Date fromDate, @Param("to") Date toDate);

  @Query(value = "SELECT COUNT(*) FROM orders WHERE payment_status = 'COMPLETED' AND DATE(created_at) BETWEEN DATE(:from) AND DATE(:to)", nativeQuery = true)
  int countSuccessOrders(@Param("from") Date fromDate, @Param("to") Date toDate);

  @Query(value = "SELECT COUNT(*) FROM orders WHERE status = 'CANCELLED' AND DATE(created_at) BETWEEN DATE(:from) AND DATE(:to)", nativeQuery = true)
  int countCancelledOrders(@Param("from") Date fromDate, @Param("to") Date toDate);

  @Query(value = "SELECT mm.type, COUNT(*) AS cnt FROM order_items oi JOIN orders o ON o.id = oi.order_id JOIN menu_meals mm ON oi.menu_meal_id = mm.id WHERE o.payment_status = 'COMPLETED' AND DATE(o.created_at) BETWEEN DATE(:from) AND DATE(:to) AND mm.type IS NOT NULL GROUP BY mm.type ORDER BY cnt DESC", nativeQuery = true)
  List<Object[]> getPopularFoods(@Param("from") Date fromDate, @Param("to") Date toDate);

  @Query(value = "SELECT o.id, o.customer_id AS customer_name, o.total_amount, o.status, o.created_at FROM orders o ORDER BY o.created_at DESC LIMIT 10", nativeQuery = true)
  List<Object[]> getRecentOrders();

  @Query(value = "SELECT mm.id, mm.title, mm.price, mm.stock, mm.type, COUNT(oi.id) AS cnt, mm.image, mm.slug FROM order_items oi JOIN orders o ON o.id = oi.order_id JOIN menu_meals mm ON oi.menu_meal_id = mm.id WHERE WEEK(o.created_at) = WEEK(:date) AND YEAR(o.created_at) = YEAR(:date) GROUP BY mm.id, mm.title, mm.price, mm.stock, mm.type, mm.image, mm.slug ORDER BY cnt DESC LIMIT 5", nativeQuery = true)
  List<Object[]> getDailyTrendingMenus(@Param("date") Date date);

  @Query(value = """
      SELECT
        CASE :type
          WHEN 'year' THEN
            CASE MONTH(created_at)
              WHEN 1 THEN 'Jan'
              WHEN 2 THEN 'Feb'
              WHEN 3 THEN 'Mar'
              WHEN 4 THEN 'Apr'
              WHEN 5 THEN 'May'
              WHEN 6 THEN 'Jun'
              WHEN 7 THEN 'Jul'
              WHEN 8 THEN 'Aug'
              WHEN 9 THEN 'Sep'
              WHEN 10 THEN 'Oct'
              WHEN 11 THEN 'Nov'
              WHEN 12 THEN 'Dec'
            END
          WHEN 'month' THEN DATE(created_at)
          ELSE DATE(created_at)
        END as period,
        COALESCE(SUM(total_amount), 0) as sales
      FROM orders
      WHERE payment_status = 'COMPLETED' AND DATE(created_at) >= DATE(:from) AND DATE(created_at) < DATE(DATE_ADD(:to, INTERVAL 1 DAY))
      GROUP BY
        CASE :type
          WHEN 'year' THEN
            CASE MONTH(created_at)
              WHEN 1 THEN 'Jan'
              WHEN 2 THEN 'Feb'
              WHEN 3 THEN 'Mar'
              WHEN 4 THEN 'Apr'
              WHEN 5 THEN 'May'
              WHEN 6 THEN 'Jun'
              WHEN 7 THEN 'Jul'
              WHEN 8 THEN 'Aug'
              WHEN 9 THEN 'Sep'
              WHEN 10 THEN 'Oct'
              WHEN 11 THEN 'Nov'
              WHEN 12 THEN 'Dec'
            END
          WHEN 'month' THEN DATE(created_at)
          ELSE DATE(created_at)
        END
      ORDER BY period ASC
      """, nativeQuery = true)
  List<Object[]> getSalesFigures(@Param("from") Date fromDate, @Param("to") Date toDate, @Param("type") String type);

  @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE DATE(created_at) = DATE(:date)", nativeQuery = true)
  double getIncomeByDate(@Param("date") Date date);
}
