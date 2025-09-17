package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.dtos.RecurringEmailScheduleRequest;
import com.greenkitchen.portal.entities.RecurringEmailSchedule;
import com.greenkitchen.portal.repositories.RecurringEmailScheduleRepository;
import com.greenkitchen.portal.services.MarketingEmailService;
import com.greenkitchen.portal.services.RecurringEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecurringEmailServiceImpl implements RecurringEmailService {

    @Autowired
    private RecurringEmailScheduleRepository repository;

    @Autowired
    private MarketingEmailService marketingEmailService;

    @Override
    public RecurringEmailSchedule create(RecurringEmailScheduleRequest req) {
        RecurringEmailSchedule s = fromReq(new RecurringEmailSchedule(), req);
        computeNextRunAt(s, LocalDateTime.now());
        return repository.save(s);
    }

    @Override
    public RecurringEmailSchedule update(Long id, RecurringEmailScheduleRequest req) {
        RecurringEmailSchedule s = repository.findById(id).orElseThrow();
        fromReq(s, req);
        computeNextRunAt(s, LocalDateTime.now());
        return repository.save(s);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<RecurringEmailSchedule> list() {
        return repository.findAll();
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void executeDueSchedules() {
        LocalDateTime now = LocalDateTime.now();
        for (RecurringEmailSchedule s : repository.findByActiveTrue()) {
            if (s.getNextRunAt() != null && !s.getNextRunAt().isAfter(now)) {
                marketingEmailService.sendBroadcast(s.getSubject(), s.getContent());
                s.setLastRunAt(now);
                computeNextRunAt(s, now);
                repository.save(s);
            }
        }
    }

    private RecurringEmailSchedule fromReq(RecurringEmailSchedule s, RecurringEmailScheduleRequest req) {
        s.setName(req.name);
        s.setFrequency(RecurringEmailSchedule.Frequency.valueOf(req.frequency));
        s.setMinuteOfHour(req.minuteOfHour);
        s.setHourOfDay(req.hourOfDay);
        s.setDayOfWeek(req.dayOfWeek != null ? DayOfWeek.valueOf(req.dayOfWeek) : null);
        s.setActive(req.active);
        s.setSubject(req.subject);
        s.setContent(req.content);
        return s;
    }

    private void computeNextRunAt(RecurringEmailSchedule s, LocalDateTime from) {
        LocalDateTime base = from.plusMinutes(1); // avoid immediate re-run
        switch (s.getFrequency()) {
            case HOURLY -> {
                int minute = s.getMinuteOfHour() != null ? s.getMinuteOfHour() : 0;
                LocalDateTime candidate = base.withSecond(0).withNano(0);
                if (candidate.getMinute() > minute) candidate = candidate.plusHours(1);
                s.setNextRunAt(candidate.withMinute(minute));
            }
            case DAILY -> {
                int hour = s.getHourOfDay() != null ? s.getHourOfDay() : 9;
                int minute = s.getMinuteOfHour() != null ? s.getMinuteOfHour() : 0;
                LocalDateTime candidate = base.withSecond(0).withNano(0).withHour(hour).withMinute(minute);
                if (!candidate.isAfter(base)) candidate = candidate.plusDays(1);
                s.setNextRunAt(candidate);
            }
            case WEEKLY -> {
                DayOfWeek dow = s.getDayOfWeek() != null ? s.getDayOfWeek() : DayOfWeek.MONDAY;
                int hour = s.getHourOfDay() != null ? s.getHourOfDay() : 9;
                int minute = s.getMinuteOfHour() != null ? s.getMinuteOfHour() : 0;
                LocalDateTime candidate = base.withSecond(0).withNano(0).withHour(hour).withMinute(minute);
                int diff = dow.getValue() - candidate.getDayOfWeek().getValue();
                if (diff < 0 || (diff == 0 && !candidate.isAfter(base))) diff += 7;
                candidate = candidate.plusDays(diff);
                s.setNextRunAt(candidate);
            }
        }
    }
}



