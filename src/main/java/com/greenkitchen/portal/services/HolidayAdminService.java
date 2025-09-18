package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.HolidayCreateRequest;
import com.greenkitchen.portal.dtos.HolidayDto;
import com.greenkitchen.portal.dtos.HolidayUpdateRequest;

import java.util.List;

public interface HolidayAdminService {
    HolidayDto create(HolidayCreateRequest req);
    HolidayDto update(Long id, HolidayUpdateRequest req);
    void delete(Long id);
    List<HolidayDto> listAll();
}




