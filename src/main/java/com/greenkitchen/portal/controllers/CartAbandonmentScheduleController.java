package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.dtos.CartAbandonmentScheduleRequest;
import com.greenkitchen.portal.dtos.CartAbandonmentScheduleResponse;
import com.greenkitchen.portal.services.CartAbandonmentScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/apis/v1/cart-abandonment-schedule")
@PreAuthorize("hasRole('ADMIN')")
public class CartAbandonmentScheduleController {

    @Autowired
    private CartAbandonmentScheduleService cartAbandonmentScheduleService;

    /**
     * Tạo lịch mới
     */
    @PostMapping
    public ResponseEntity<?> createSchedule(@Valid @RequestBody CartAbandonmentScheduleRequest request) {
        try {
            CartAbandonmentScheduleResponse response = cartAbandonmentScheduleService.createSchedule(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cập nhật lịch
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @Valid @RequestBody CartAbandonmentScheduleRequest request) {
        try {
            CartAbandonmentScheduleResponse response = cartAbandonmentScheduleService.updateSchedule(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy tất cả lịch
     */
    @GetMapping
    public ResponseEntity<List<CartAbandonmentScheduleResponse>> getAllSchedules() {
        List<CartAbandonmentScheduleResponse> schedules = cartAbandonmentScheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lấy lịch theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getScheduleById(@PathVariable Long id) {
        try {
            CartAbandonmentScheduleResponse response = cartAbandonmentScheduleService.getScheduleById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xóa lịch
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        try {
            cartAbandonmentScheduleService.deleteSchedule(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa lịch thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa lịch
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleSchedule(@PathVariable Long id) {
        try {
            CartAbandonmentScheduleResponse response = cartAbandonmentScheduleService.toggleSchedule(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kiểm tra tên lịch đã tồn tại chưa
     */
    @GetMapping("/check-name")
    public ResponseEntity<?> checkScheduleName(@RequestParam String scheduleName, @RequestParam(required = false) Long excludeId) {
        boolean exists = cartAbandonmentScheduleService.isScheduleNameExists(scheduleName, excludeId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Lấy thống kê lịch
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getScheduleStatistics() {
        try {
            List<CartAbandonmentScheduleResponse> allSchedules = cartAbandonmentScheduleService.getAllSchedules();
            long activeCount = allSchedules.stream().mapToLong(s -> s.getIsActive() ? 1 : 0).sum();
            long dailyEnabledCount = allSchedules.stream().mapToLong(s -> s.getIsDailyEnabled() ? 1 : 0).sum();
            long intervalEnabledCount = allSchedules.stream().mapToLong(s -> s.getIsIntervalEnabled() ? 1 : 0).sum();
            long eveningEnabledCount = allSchedules.stream().mapToLong(s -> s.getIsEveningEnabled() ? 1 : 0).sum();

            return ResponseEntity.ok(Map.of(
                "totalSchedules", allSchedules.size(),
                "activeSchedules", activeCount,
                "dailyEnabledSchedules", dailyEnabledCount,
                "intervalEnabledSchedules", intervalEnabledCount,
                "eveningEnabledSchedules", eveningEnabledCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
