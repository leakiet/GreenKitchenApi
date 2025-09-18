package com.greenkitchen.portal.entities;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_email_schedules")
public class RecurringEmailSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Frequency frequency;

    private Integer minuteOfHour;
    private Integer hourOfDay;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, length = 10000)
    private String content;

    private LocalDateTime lastRunAt;
    private LocalDateTime nextRunAt;

    public enum Frequency { HOURLY, DAILY, WEEKLY }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }
    public Integer getMinuteOfHour() { return minuteOfHour; }
    public void setMinuteOfHour(Integer minuteOfHour) { this.minuteOfHour = minuteOfHour; }
    public Integer getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(Integer hourOfDay) { this.hourOfDay = hourOfDay; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(LocalDateTime lastRunAt) { this.lastRunAt = lastRunAt; }
    public LocalDateTime getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(LocalDateTime nextRunAt) { this.nextRunAt = nextRunAt; }
}



