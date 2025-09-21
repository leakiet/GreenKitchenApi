package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.dtos.HolidayCreateRequest;
import com.greenkitchen.portal.dtos.HolidayDto;
import com.greenkitchen.portal.dtos.HolidayUpdateRequest;
import com.greenkitchen.portal.entities.Holiday;
import com.greenkitchen.portal.repositories.HolidayRepository;
import com.greenkitchen.portal.services.HolidayAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class HolidayAdminServiceImpl implements HolidayAdminService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Override
    public HolidayDto getById(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Holiday không tồn tại"));
        return toDto(holiday);
    }

    @Override
    public HolidayDto create(HolidayCreateRequest req) {
        Holiday h = new Holiday();
        h.setName(req.name);
        h.setCountry(req.country);
        h.setDate(req.date);
        h.setLunar(req.lunar);
        h.setRecurrenceType(Holiday.RecurrenceType.valueOf(req.recurrenceType));
        h.setDescription(req.description);
        Holiday saved = holidayRepository.save(h);
        return toDto(saved);
    }

    @Override
    public HolidayDto update(Long id, HolidayUpdateRequest req) {
        Holiday h = holidayRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Holiday not found"));
        h.setName(req.name);
        h.setCountry(req.country);
        h.setDate(req.date);
        h.setLunar(req.lunar);
        h.setRecurrenceType(Holiday.RecurrenceType.valueOf(req.recurrenceType));
        h.setDescription(req.description);
        Holiday saved = holidayRepository.save(h);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        holidayRepository.deleteById(id);
    }

    @Override
    public List<HolidayDto> listAll() {
        return holidayRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private HolidayDto toDto(Holiday h) {
        HolidayDto dto = new HolidayDto();
        dto.id = h.getId();
        dto.name = h.getName();
        dto.country = h.getCountry();
        dto.date = h.getDate();
        dto.lunar = h.isLunar();
        dto.recurrenceType = h.getRecurrenceType().name();
        dto.description = h.getDescription();
        dto.daysUntil = 0; // not computed here
        return dto;
    }
}






