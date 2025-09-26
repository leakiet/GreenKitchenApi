package com.greenkitchen.portal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.CartScanResponse;
import com.greenkitchen.portal.entities.CartAbandonmentSchedule;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
public class CartAbandonmentScheduleExecutorService {

    @Autowired
    private CartScanService cartScanService;

    @Autowired
    private CartAbandonmentScheduleService scheduleService;

    /**
     * Chạy mỗi phút để kiểm tra và thực hiện các lịch cart abandonment đã được cấu hình
     * Thay thế cho các @Scheduled cố định trước đây
     */
    @Scheduled(fixedRate = 60000) // Mỗi phút
    public void processCartAbandonmentSchedules() {
        try {
            List<CartAbandonmentSchedule> activeSchedules = scheduleService.getActiveSchedules();
            java.time.ZoneId zone = java.time.ZoneId.systemDefault();
            java.time.LocalDateTime nowTs = java.time.LocalDateTime.now(zone);
            log.info("[Scheduler Tick] now={} zone={}", nowTs, zone);
            
            if (activeSchedules.isEmpty()) {
                log.info("Không có lịch cart abandonment nào đang hoạt động");
                return;
            }

            for (CartAbandonmentSchedule schedule : activeSchedules) {

                boolean should = shouldExecuteSchedule(schedule);
                log.debug("Result shouldExecute = {} for schedule id={}", should, schedule.getId());
                if (should) {
                    executeCartAbandonmentSchedule(schedule);
                } else {
                    log.trace("Skip schedule id={} at now={}", schedule.getId(), nowTs);
                }
            }
        } catch (Exception e) {
            log.error("❌ Lỗi trong quá trình xử lý lịch cart abandonment: {}", e.getMessage());
        }
    }

    /**
     * Kiểm tra xem lịch có nên được thực hiện không
     */
    private boolean shouldExecuteSchedule(CartAbandonmentSchedule schedule) {
        // Ưu tiên theo cấu hình bật/tắt: daily, evening, interval
        if (Boolean.TRUE.equals(schedule.getIsDailyEnabled()) && schedule.getDailyTime() != null) {
            return isTimeToExecuteDaily(schedule.getDailyTime());
        }

        if (Boolean.TRUE.equals(schedule.getIsEveningEnabled()) && schedule.getEveningTime() != null) {
            return isTimeToExecuteDaily(schedule.getEveningTime());
        }

        if (Boolean.TRUE.equals(schedule.getIsIntervalEnabled()) && schedule.getIntervalHours() != null) {
            return isTimeToExecuteHourly(schedule.getIntervalHours());
        }

        return false;
    }

    /**
     * Kiểm tra xem có phải thời gian thực hiện lịch hàng ngày không
     */
    private boolean isTimeToExecuteDaily(java.time.LocalTime scheduleTime) {
        if (scheduleTime == null) return false;
        
        java.time.LocalTime now = java.time.LocalTime.now();
        boolean inWindow = now.isAfter(scheduleTime.minusMinutes(1)) && now.isBefore(scheduleTime.plusMinutes(1));
        log.debug("isTimeToExecuteDaily? now={} scheduleTime={} window=[{}, {}] => {}",
                now, scheduleTime, scheduleTime.minusMinutes(1), scheduleTime.plusMinutes(1), inWindow);
        return inWindow;
    }

    /**
     * Kiểm tra xem có phải thời gian thực hiện lịch theo giờ không
     */
    private boolean isTimeToExecuteHourly(Integer frequencyHours) {
        if (frequencyHours == null || frequencyHours <= 0) return false;
        
        java.time.LocalTime now = java.time.LocalTime.now();
        int currentHour = now.getHour();
        
        // Kiểm tra xem giờ hiện tại có chia hết cho frequency không
        boolean match = currentHour % frequencyHours == 0 && now.getMinute() == 0;
        log.debug("isTimeToExecuteHourly? now={} freqHours={} => {}", now, frequencyHours, match);
        return match;
    }

    /**
     * Thực hiện lịch cart abandonment
     */
    private void executeCartAbandonmentSchedule(CartAbandonmentSchedule schedule) {
        log.info("🕘 Bắt đầu thực hiện lịch cart abandonment: {}", 
                schedule.getScheduleName());
        
        try {
            CartScanResponse response = cartScanService.scanAndSendEmails();
            log.info("✅ Lịch '{}' hoàn thành: {} customers được gửi email", 
                    schedule.getScheduleName(), response.getNewCustomersFound());
        } catch (Exception e) {
            log.error("❌ Lỗi trong lịch '{}': {}", schedule.getScheduleName(), e.getMessage());
        }
    }

    /**
     * Manual trigger để test lịch (có thể gọi từ API)
     */
    public void triggerCartAbandonmentSchedule(Long scheduleId) {
        try {
            List<CartAbandonmentSchedule> active = scheduleService.getActiveSchedules();
            CartAbandonmentSchedule schedule = active.stream()
                .filter(s -> s.getId().equals(scheduleId))
                .findFirst()
                .orElse(null);

            if (schedule == null) {
                log.warn("Không tìm thấy lịch hoạt động với ID {}", scheduleId);
                return;
            }

            if (!Boolean.TRUE.equals(schedule.getIsActive())) {
                log.warn("Lịch '{}' đang bị tắt", schedule.getScheduleName());
                return;
            }

            executeCartAbandonmentSchedule(schedule);
        } catch (Exception e) {
            log.error("❌ Lỗi khi trigger lịch ID {}: {}", scheduleId, e.getMessage());
        }
    }
}
