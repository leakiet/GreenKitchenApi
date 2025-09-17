package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.RecurringEmailScheduleRequest;
import com.greenkitchen.portal.entities.RecurringEmailSchedule;

import java.util.List;

public interface RecurringEmailService {
    RecurringEmailSchedule create(RecurringEmailScheduleRequest req);
    RecurringEmailSchedule update(Long id, RecurringEmailScheduleRequest req);
    void delete(Long id);
    List<RecurringEmailSchedule> list();
    void executeDueSchedules();
}



