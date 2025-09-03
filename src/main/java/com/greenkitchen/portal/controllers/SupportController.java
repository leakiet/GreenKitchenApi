package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.FeedbackRequest;
import com.greenkitchen.portal.dtos.SupportRequest;
import com.greenkitchen.portal.services.EmailService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/apis/v1/support")
@CrossOrigin(origins = "${app.frontend.url}", allowCredentials = "true")
public class SupportController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        emailService.sendFeedbackEmail(
            request.getFromEmail(),
            request.getType(),
            request.getRating(),
            request.getTitle(),
            request.getDescription(),
            request.getContactEmail()
        );
        return ResponseEntity.ok("Feedback submitted");
    }

    @PostMapping("/ticket")
    public ResponseEntity<?> submitSupportTicket(@Valid @RequestBody SupportRequest request) {
        emailService.sendSupportRequestEmail(
            request.getIssueType(),
            request.getPriority(),
            request.getSubject(),
            request.getDescription(),
            request.getContactMethod(),
            request.getContactValue()
        );
        return ResponseEntity.ok("Support ticket submitted");
    }
}


