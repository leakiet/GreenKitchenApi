package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.HolidayDto;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {
    List<HolidayDto> listUpcoming(LocalDate fromDate, int daysAhead);
}


