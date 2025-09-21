package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.dtos.EmailTemplate;
import com.greenkitchen.portal.dtos.HolidayEmailScheduleRequest;
import com.greenkitchen.portal.dtos.HolidayDto;
import com.greenkitchen.portal.entities.ScheduledHolidayEmail;
import com.greenkitchen.portal.services.HolidayAdminService;
import com.greenkitchen.portal.services.HolidayEmailTemplateService;
import com.greenkitchen.portal.services.MarketingEmailService;
import com.greenkitchen.portal.services.ScheduledHolidayEmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/apis/v1/holidays/email")
public class HolidayEmailController {

    @Autowired
    private HolidayEmailTemplateService holidayEmailTemplateService;
    
    @Autowired
    private HolidayAdminService holidayAdminService;
    
    @Autowired
    private MarketingEmailService marketingEmailService;
    
    @Autowired
    private ScheduledHolidayEmailService scheduledHolidayEmailService;

    /**
     * Get email template for a specific holiday
     */
    @GetMapping("/templates/{holidayId}")
    public ResponseEntity<?> getHolidayTemplate(@PathVariable("holidayId") Long holidayId) {
        try {
            // Get holiday from database
            HolidayDto holidayDto = holidayAdminService.getById(holidayId);
            if (holidayDto == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Generate template directly from DTO
            EmailTemplate template = holidayEmailTemplateService.generateTemplate(holidayDto);
            return ResponseEntity.ok(template);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get email template for a specific holiday with custom template type
     */
    @GetMapping("/templates/{holidayId}/{templateType}")
    public ResponseEntity<?> getHolidayTemplateWithType(
            @PathVariable("holidayId") Long holidayId, 
            @PathVariable("templateType") String templateType) {
        try {
            // Get holiday from database
            HolidayDto holidayDto = holidayAdminService.getById(holidayId);
            if (holidayDto == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Generate template with specific type directly from DTO
            EmailTemplate template = holidayEmailTemplateService.generateTemplate(holidayDto, templateType);
            return ResponseEntity.ok(template);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Schedule holiday email
     */
    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleHolidayEmail(@Valid @RequestBody HolidayEmailScheduleRequest request) {
        try {
            // Get holiday from database
            HolidayDto holidayDto = holidayAdminService.getById(request.getHolidayId());
            if (holidayDto == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Holiday not found"));
            }
            
            // Generate email template directly from DTO
            EmailTemplate template = holidayEmailTemplateService.generateTemplate(holidayDto);
            
            // Use custom subject/content if provided
            String subject = request.getCustomSubject() != null ? request.getCustomSubject() : template.getSubject();
            String content = request.getCustomContent() != null ? request.getCustomContent() : template.getContent();
            
            // Replace placeholders in content
            content = replacePlaceholders(content, holidayDto);
            subject = replacePlaceholders(subject, holidayDto);
            
            // Save scheduled email to database
            ScheduledHolidayEmail scheduledEmail = scheduledHolidayEmailService.createScheduledEmail(
                request, 
                holidayDto.name, 
                holidayDto.date.atStartOfDay()
            );
            
            // Schedule email in marketing service
            marketingEmailService.scheduleBroadcast(subject, content, request.getScheduleAt());
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Holiday email scheduled successfully");
            response.put("scheduledEmailId", scheduledEmail.getId());
            response.put("holidayName", holidayDto.name);
            response.put("holidayDate", holidayDto.date);
            response.put("scheduleAt", request.getScheduleAt());
            response.put("subject", subject);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Send immediate holiday email (for testing)
     */
    @PostMapping("/send-immediate")
    public ResponseEntity<?> sendImmediateHolidayEmail(@Valid @RequestBody HolidayEmailScheduleRequest request) {
        try {
            // Get holiday from database
            HolidayDto holidayDto = holidayAdminService.getById(request.getHolidayId());
            if (holidayDto == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Holiday not found"));
            }
            
            // Generate email template directly from DTO
            EmailTemplate template = holidayEmailTemplateService.generateTemplate(holidayDto);
            
            // Use custom subject/content if provided
            String subject = request.getCustomSubject() != null ? request.getCustomSubject() : template.getSubject();
            String content = request.getCustomContent() != null ? request.getCustomContent() : template.getContent();
            
            // Replace placeholders in content
            content = replacePlaceholders(content, holidayDto);
            subject = replacePlaceholders(subject, holidayDto);
            
            // Send immediate email
            int sentCount = marketingEmailService.sendBroadcast(subject, content);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Holiday email sent immediately");
            response.put("holidayName", holidayDto.name);
            response.put("holidayDate", holidayDto.date);
            response.put("sentCount", sentCount);
            response.put("subject", subject);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get available template types
     */
    @GetMapping("/template-types")
    public ResponseEntity<?> getAvailableTemplateTypes() {
        try {
            String[] templateTypes = holidayEmailTemplateService.getAvailableTemplateTypes();
            return ResponseEntity.ok(Map.of("templateTypes", templateTypes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all scheduled holiday emails
     */
    @GetMapping("/scheduled")
    public ResponseEntity<?> getScheduledHolidayEmails() {
        try {
            List<ScheduledHolidayEmail> scheduledEmails = scheduledHolidayEmailService.getAllScheduledEmails();
            return ResponseEntity.ok(scheduledEmails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete scheduled holiday email
     */
    @DeleteMapping("/scheduled/{scheduleId}")
    public ResponseEntity<?> deleteScheduledHolidayEmail(@PathVariable("scheduleId") Long scheduleId) {
        try {
            scheduledHolidayEmailService.deleteScheduledEmail(scheduleId);
            return ResponseEntity.ok(Map.of("message", "Scheduled email deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update scheduled holiday email
     */
    @PutMapping("/scheduled/{scheduleId}")
    public ResponseEntity<?> updateScheduledHolidayEmail(
            @PathVariable("scheduleId") Long scheduleId,
            @Valid @RequestBody HolidayEmailScheduleRequest request) {
        try {
            ScheduledHolidayEmail updatedEmail = scheduledHolidayEmailService.updateScheduledEmail(scheduleId, request);
            return ResponseEntity.ok(Map.of(
                "message", "Scheduled email updated successfully",
                "scheduledEmailId", updatedEmail.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Replace placeholders in email content
     */
    private String replacePlaceholders(String content, HolidayDto holiday) {
        if (content == null) return "";
        
        return content
            .replace("{{customerName}}", "Quý khách")
            .replace("{{holidayName}}", holiday.name)
            .replace("{{holidayDate}}", holiday.date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy", java.util.Locale.forLanguageTag("vi-VN"))))
            .replace("{{frontendUrl}}", "https://greenkitchen.com"); // TODO: Get from config
    }
}
