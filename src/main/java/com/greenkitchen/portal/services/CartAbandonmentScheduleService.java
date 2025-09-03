package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.CartAbandonmentScheduleRequest;
import com.greenkitchen.portal.dtos.CartAbandonmentScheduleResponse;
import com.greenkitchen.portal.entities.CartAbandonmentSchedule;

import java.util.List;

public interface CartAbandonmentScheduleService {
    
    /**
     * Tạo lịch mới
     */
    CartAbandonmentScheduleResponse createSchedule(CartAbandonmentScheduleRequest request);
    
    /**
     * Cập nhật lịch
     */
    CartAbandonmentScheduleResponse updateSchedule(Long id, CartAbandonmentScheduleRequest request);
    
    /**
     * Lấy tất cả lịch
     */
    List<CartAbandonmentScheduleResponse> getAllSchedules();
    
    /**
     * Lấy lịch theo ID
     */
    CartAbandonmentScheduleResponse getScheduleById(Long id);
    
    /**
     * Xóa lịch
     */
    void deleteSchedule(Long id);
    
    /**
     * Kích hoạt/vô hiệu hóa lịch
     */
    CartAbandonmentScheduleResponse toggleSchedule(Long id);
    
    /**
     * Lấy lịch đang hoạt động
     */
    List<CartAbandonmentSchedule> getActiveSchedules();
    
    /**
     * Lấy lịch hàng ngày đang hoạt động
     */
    List<CartAbandonmentSchedule> getActiveDailySchedules();
    
    /**
     * Lấy lịch theo khoảng cách đang hoạt động
     */
    List<CartAbandonmentSchedule> getActiveIntervalSchedules();
    
    /**
     * Lấy lịch buổi tối đang hoạt động
     */
    List<CartAbandonmentSchedule> getActiveEveningSchedules();
    
    /**
     * Kiểm tra tên lịch đã tồn tại chưa
     */
    boolean isScheduleNameExists(String scheduleName, Long excludeId);
}
