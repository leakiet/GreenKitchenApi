package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.dtos.HolidayDto;
import com.greenkitchen.portal.services.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/apis/v1/holidays")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @GetMapping("/upcoming")
    public ResponseEntity<List<HolidayDto>> upcoming(
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(name = "daysAhead", defaultValue = "120") int daysAhead
    ) {
        return ResponseEntity.ok(holidayService.listUpcoming(fromDate, daysAhead));
    }
}


