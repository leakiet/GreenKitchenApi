package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.dtos.RecurringEmailScheduleRequest;
import com.greenkitchen.portal.entities.RecurringEmailSchedule;
import com.greenkitchen.portal.services.RecurringEmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/apis/v1/recurring-emails")
public class RecurringEmailController {

    @Autowired
    private RecurringEmailService recurringEmailService;

    @GetMapping
    public ResponseEntity<List<RecurringEmailSchedule>> list() {
        return ResponseEntity.ok(recurringEmailService.list());
    }

    @PostMapping
    public ResponseEntity<RecurringEmailSchedule> create(@Valid @RequestBody RecurringEmailScheduleRequest req) {
        return ResponseEntity.ok(recurringEmailService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringEmailSchedule> update(@PathVariable(name = "id") Long id, @Valid @RequestBody RecurringEmailScheduleRequest req) {
        return ResponseEntity.ok(recurringEmailService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        recurringEmailService.delete(id);
        return ResponseEntity.noContent().build();
    }
}



