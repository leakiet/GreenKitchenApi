package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class HolidayCreateRequest {
    @NotBlank
    public String name;
    public String country;
    @NotNull
    public LocalDate date;
    public boolean lunar;
    @NotBlank
    public String recurrenceType;
    public String description;
}




