package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.entities.ScheduledEmail;
import com.greenkitchen.portal.services.ScheduledEmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/emails")
public class EmailScheduleController {

    @Autowired
    private ScheduledEmailService scheduledEmailService;

    public static class ScheduleEmailRequest {
        @NotBlank
        @Email
        public String recipient;

        @NotBlank
        public String subject;

        @NotBlank
        public String content;

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        public LocalDateTime sendAt;
    }

    @PostMapping("/schedule")
    public ResponseEntity<ScheduledEmail> schedule(@Valid @RequestBody ScheduleEmailRequest req) {
        ScheduledEmail email = new ScheduledEmail();
        email.setRecipient(req.recipient);
        email.setSubject(req.subject);
        email.setContent(req.content);
        email.setSendAt(req.sendAt);
        ScheduledEmail saved = scheduledEmailService.scheduleEmail(email);
        return ResponseEntity.ok(saved);
    }
}


