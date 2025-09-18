package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.dtos.HolidayDto;
import com.greenkitchen.portal.entities.Holiday;
import com.greenkitchen.portal.repositories.HolidayRepository;
import com.greenkitchen.portal.services.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Override
    public List<HolidayDto> listUpcoming(LocalDate fromDate, int daysAhead) {
        LocalDate start = fromDate != null ? fromDate : LocalDate.now();
        LocalDate end = start.plusDays(daysAhead <= 0 ? 365 : daysAhead);

        List<Holiday> holidays = holidayRepository.findByDateBetweenOrderByDateAsc(start, end);
        return holidays.stream().map(h -> toDto(h, start)).collect(Collectors.toList());
    }

    private HolidayDto toDto(Holiday h, LocalDate from) {
        HolidayDto dto = new HolidayDto();
        dto.id = h.getId();
        dto.name = h.getName();
        dto.country = h.getCountry();
        dto.date = h.getDate();
        dto.lunar = h.isLunar();
        dto.recurrenceType = h.getRecurrenceType().name();
        dto.description = h.getDescription();
        dto.daysUntil = ChronoUnit.DAYS.between(from, h.getDate());
        return dto;
    }
}


