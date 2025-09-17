package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.RecurringEmailSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecurringEmailScheduleRepository extends JpaRepository<RecurringEmailSchedule, Long> {
    List<RecurringEmailSchedule> findByActiveTrue();
}


