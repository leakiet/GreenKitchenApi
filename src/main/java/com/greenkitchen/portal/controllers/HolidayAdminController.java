package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.dtos.HolidayCreateRequest;
import com.greenkitchen.portal.dtos.HolidayDto;
import com.greenkitchen.portal.dtos.HolidayUpdateRequest;
import com.greenkitchen.portal.services.HolidayAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/apis/v1/holidays/admin")
public class HolidayAdminController {

    @Autowired
    private HolidayAdminService holidayAdminService;

    @GetMapping
    public ResponseEntity<List<HolidayDto>> listAll() {
        return ResponseEntity.ok(holidayAdminService.listAll());
    }

    @PostMapping
    public ResponseEntity<HolidayDto> create(@Valid @RequestBody HolidayCreateRequest req) {
        return ResponseEntity.ok(holidayAdminService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HolidayDto> update(@PathVariable(name = "id") Long id, @Valid @RequestBody HolidayUpdateRequest req) {
        return ResponseEntity.ok(holidayAdminService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        holidayAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}




