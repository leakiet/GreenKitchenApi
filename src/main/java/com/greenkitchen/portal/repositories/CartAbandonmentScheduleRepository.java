package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.CartAbandonmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartAbandonmentScheduleRepository extends JpaRepository<CartAbandonmentSchedule, Long> {
    
    // Tìm lịch đang hoạt động
    @Query("SELECT c FROM CartAbandonmentSchedule c WHERE c.isActive = true")
    List<CartAbandonmentSchedule> findActiveSchedules();
    
    // Tìm lịch theo tên
    Optional<CartAbandonmentSchedule> findByScheduleName(String scheduleName);
    
    // Kiểm tra tên lịch đã tồn tại chưa (trừ id hiện tại)
    @Query("SELECT COUNT(c) > 0 FROM CartAbandonmentSchedule c WHERE c.scheduleName = :scheduleName AND c.id != :id")
    boolean existsByScheduleNameAndIdNot(@Param("scheduleName") String scheduleName, @Param("id") Long id);
    
    // Tìm lịch hàng ngày đang hoạt động
    @Query("SELECT c FROM CartAbandonmentSchedule c WHERE c.isActive = true AND c.isDailyEnabled = true")
    List<CartAbandonmentSchedule> findActiveDailySchedules();
    
    // Tìm lịch theo khoảng cách đang hoạt động
    @Query("SELECT c FROM CartAbandonmentSchedule c WHERE c.isActive = true AND c.isIntervalEnabled = true")
    List<CartAbandonmentSchedule> findActiveIntervalSchedules();
    
    // Tìm lịch buổi tối đang hoạt động
    @Query("SELECT c FROM CartAbandonmentSchedule c WHERE c.isActive = true AND c.isEveningEnabled = true")
    List<CartAbandonmentSchedule> findActiveEveningSchedules();
    
    // Đếm số lịch đang hoạt động
    @Query("SELECT COUNT(c) FROM CartAbandonmentSchedule c WHERE c.isActive = true")
    long countActiveSchedules();
}
