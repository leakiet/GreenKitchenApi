package com.greenkitchen.portal.dtos;

import java.time.LocalDate;

public class HolidayDto {
    public Long id;
    public String name;
    public String country;
    public LocalDate date;
    public boolean lunar;
    public String recurrenceType;
    public String description;
    public long daysUntil;
}


