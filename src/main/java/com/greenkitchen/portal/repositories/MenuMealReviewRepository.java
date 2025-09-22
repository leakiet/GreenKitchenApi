package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenkitchen.portal.entities.MenuMealReview;

public interface MenuMealReviewRepository extends JpaRepository<MenuMealReview, Long> {

    // Tìm tất cả review theo MenuMeal ID
    List<MenuMealReview> findByMenuMealId(Long menuMealId);
    
    // Tìm tất cả review theo MenuMeal ID với eager loading
    @Query("SELECT r FROM MenuMealReview r JOIN FETCH r.customer JOIN FETCH r.menuMeal WHERE r.menuMeal.id = :menuMealId")
    List<MenuMealReview> findByMenuMealIdWithCustomerAndMenuMeal(@Param("menuMealId") Long menuMealId);

    // Tìm tất cả review theo Customer ID
    List<MenuMealReview> findByCustomerId(Long customerId);
    
    // Tìm tất cả review theo Customer ID với eager loading
    @Query("SELECT r FROM MenuMealReview r JOIN FETCH r.customer JOIN FETCH r.menuMeal WHERE r.customer.id = :customerId")
    List<MenuMealReview> findByCustomerIdWithCustomerAndMenuMeal(@Param("customerId") Long customerId);

    // Kiểm tra customer đã review menu meal này chưa
    boolean existsByMenuMealIdAndCustomerId(Long menuMealId, Long customerId);

    // Tính average rating cho menu meal
    @Query("SELECT AVG(r.rating) FROM MenuMealReview r WHERE r.menuMeal.id = :menuMealId")
    Double getAverageRatingByMenuMealId(@Param("menuMealId") Long menuMealId);

    // Đếm số review cho menu meal
    Long countByMenuMealId(Long menuMealId);

    // Thêm method cho filtering với pagination
    @Query("SELECT r FROM MenuMealReview r WHERE " +
           "(:status IS NULL OR r.rating = :status) AND " +
           "(:q IS NULL OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<MenuMealReview> findAllFiltered(Pageable pageable, @Param("status") String status, @Param("q") String q);

    // Thêm method cho filtering theo menuMealId
    @Query("SELECT r FROM MenuMealReview r WHERE r.menuMeal.id = :menuMealId AND " +
           "(:status IS NULL OR r.rating = :status) AND " +
           "(:q IS NULL OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<MenuMealReview> findFilteredByMenuMealId(@Param("menuMealId") Long menuMealId, Pageable pageable, 
                                                  @Param("status") String status, @Param("q") String q);
}