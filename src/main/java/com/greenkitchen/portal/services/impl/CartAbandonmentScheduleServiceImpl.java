package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.dtos.CartAbandonmentScheduleRequest;
import com.greenkitchen.portal.dtos.CartAbandonmentScheduleResponse;
import com.greenkitchen.portal.entities.CartAbandonmentSchedule;
import com.greenkitchen.portal.repositories.CartAbandonmentScheduleRepository;
import com.greenkitchen.portal.services.CartAbandonmentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartAbandonmentScheduleServiceImpl implements CartAbandonmentScheduleService {

    @Autowired
    private CartAbandonmentScheduleRepository cartAbandonmentScheduleRepository;

    @Override
    public CartAbandonmentScheduleResponse createSchedule(CartAbandonmentScheduleRequest request) {
        // Kiểm tra tên lịch đã tồn tại chưa
        if (cartAbandonmentScheduleRepository.findByScheduleName(request.getScheduleName()).isPresent()) {
            throw new RuntimeException("Tên lịch đã tồn tại: " + request.getScheduleName());
        }

        CartAbandonmentSchedule schedule = new CartAbandonmentSchedule();
        schedule.setScheduleName(request.getScheduleName());
        schedule.setDailyTime(request.getDailyTime());
        schedule.setIntervalHours(request.getIntervalHours());
        schedule.setIsDailyEnabled(request.getIsDailyEnabled());
        schedule.setIsIntervalEnabled(request.getIsIntervalEnabled());
        schedule.setIsEveningEnabled(request.getIsEveningEnabled());
        schedule.setEveningTime(request.getEveningTime());
        schedule.setDescription(request.getDescription());
        schedule.setIsActive(true);
        schedule.setCreatedBy("admin"); // TODO: Lấy từ authentication

        CartAbandonmentSchedule savedSchedule = cartAbandonmentScheduleRepository.save(schedule);
        return convertToResponse(savedSchedule);
    }

    @Override
    public CartAbandonmentScheduleResponse updateSchedule(Long id, CartAbandonmentScheduleRequest request) {
        CartAbandonmentSchedule schedule = cartAbandonmentScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch với ID: " + id));

        // Kiểm tra tên lịch đã tồn tại chưa (trừ lịch hiện tại)
        if (cartAbandonmentScheduleRepository.existsByScheduleNameAndIdNot(request.getScheduleName(), id)) {
            throw new RuntimeException("Tên lịch đã tồn tại: " + request.getScheduleName());
        }

        schedule.setScheduleName(request.getScheduleName());
        schedule.setDailyTime(request.getDailyTime());
        schedule.setIntervalHours(request.getIntervalHours());
        schedule.setIsDailyEnabled(request.getIsDailyEnabled());
        schedule.setIsIntervalEnabled(request.getIsIntervalEnabled());
        schedule.setIsEveningEnabled(request.getIsEveningEnabled());
        schedule.setEveningTime(request.getEveningTime());
        schedule.setDescription(request.getDescription());
        schedule.setUpdatedAt(LocalDateTime.now());

        CartAbandonmentSchedule savedSchedule = cartAbandonmentScheduleRepository.save(schedule);
        return convertToResponse(savedSchedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartAbandonmentScheduleResponse> getAllSchedules() {
        List<CartAbandonmentSchedule> schedules = cartAbandonmentScheduleRepository.findAll();
        return schedules.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CartAbandonmentScheduleResponse getScheduleById(Long id) {
        CartAbandonmentSchedule schedule = cartAbandonmentScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch với ID: " + id));
        return convertToResponse(schedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        CartAbandonmentSchedule schedule = cartAbandonmentScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch với ID: " + id));
        cartAbandonmentScheduleRepository.delete(schedule);
    }

    @Override
    public CartAbandonmentScheduleResponse toggleSchedule(Long id) {
        CartAbandonmentSchedule schedule = cartAbandonmentScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch với ID: " + id));
        
        schedule.setIsActive(!schedule.getIsActive());
        schedule.setUpdatedAt(LocalDateTime.now());
        
        CartAbandonmentSchedule savedSchedule = cartAbandonmentScheduleRepository.save(schedule);
        return convertToResponse(savedSchedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartAbandonmentSchedule> getActiveSchedules() {
        return cartAbandonmentScheduleRepository.findActiveSchedules();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartAbandonmentSchedule> getActiveDailySchedules() {
        return cartAbandonmentScheduleRepository.findActiveDailySchedules();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartAbandonmentSchedule> getActiveIntervalSchedules() {
        return cartAbandonmentScheduleRepository.findActiveIntervalSchedules();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartAbandonmentSchedule> getActiveEveningSchedules() {
        return cartAbandonmentScheduleRepository.findActiveEveningSchedules();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isScheduleNameExists(String scheduleName, Long excludeId) {
        if (excludeId == null) {
            return cartAbandonmentScheduleRepository.findByScheduleName(scheduleName).isPresent();
        }
        return cartAbandonmentScheduleRepository.existsByScheduleNameAndIdNot(scheduleName, excludeId);
    }

    private CartAbandonmentScheduleResponse convertToResponse(CartAbandonmentSchedule schedule) {
        CartAbandonmentScheduleResponse response = new CartAbandonmentScheduleResponse();
        response.setId(schedule.getId());
        response.setScheduleName(schedule.getScheduleName());
        response.setIsActive(schedule.getIsActive());
        response.setDailyTime(schedule.getDailyTime());
        response.setIntervalHours(schedule.getIntervalHours());
        response.setIsDailyEnabled(schedule.getIsDailyEnabled());
        response.setIsIntervalEnabled(schedule.getIsIntervalEnabled());
        response.setIsEveningEnabled(schedule.getIsEveningEnabled());
        response.setEveningTime(schedule.getEveningTime());
        response.setDescription(schedule.getDescription());
        response.setCreatedAt(schedule.getCreatedAt());
        response.setUpdatedAt(schedule.getUpdatedAt());
        response.setCreatedBy(schedule.getCreatedBy());
        return response;
    }
}
