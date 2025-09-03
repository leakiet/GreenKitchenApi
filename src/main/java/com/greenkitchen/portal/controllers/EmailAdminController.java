package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.BroadcastEmailRequest;
import com.greenkitchen.portal.dtos.BroadcastScheduleRequest;
import com.greenkitchen.portal.entities.EmailHistory;
import com.greenkitchen.portal.services.EmailHistoryService;
import com.greenkitchen.portal.services.EmailSchedulerService;
import com.greenkitchen.portal.services.MarketingEmailService;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/apis/v1/email-admin")
public class EmailAdminController {

    @Autowired
    private MarketingEmailService marketingEmailService;
    
    @Autowired
    private EmailHistoryService emailHistoryService;
    
    @Autowired
    private EmailSchedulerService emailSchedulerService;

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(@Valid @RequestBody BroadcastEmailRequest req) {
        int sent = marketingEmailService.sendBroadcast(req.getSubject(), req.getContent());
        
        // Lưu lịch sử email
        EmailHistory history = new EmailHistory(
            req.getSubject(), 
            req.getContent(), 
            "broadcast", 
            "sent", 
            "admin" // TODO: lấy từ authentication
        );
        history.setTotalSent(sent);
        history.setSentAt(LocalDateTime.now());
        emailHistoryService.saveEmailHistory(history);
        
        return ResponseEntity.ok(java.util.Map.of("sent", sent));
    }

    @PostMapping("/broadcast-schedule")
    public ResponseEntity<?> broadcastSchedule(@Valid @RequestBody BroadcastScheduleRequest req) {
        marketingEmailService.scheduleBroadcast(req.getSubject(), req.getContent(), req.getScheduleAt());
        
        // Lưu lịch sử email đã lên lịch
        EmailHistory history = new EmailHistory(
            req.getSubject(), 
            req.getContent(), 
            "broadcast", 
            "scheduled", 
            "admin" // TODO: lấy từ authentication
        );
        history.setScheduledAt(req.getScheduleAt());
        emailHistoryService.saveEmailHistory(history);
        
        return ResponseEntity.ok(java.util.Map.of(
            "scheduled", true,
            "runAt", req.getScheduleAt()
        ));
    }

    @PostMapping("/preview")
    public ResponseEntity<?> preview(@RequestParam("to") String to, @Valid @RequestBody BroadcastEmailRequest req) {
        marketingEmailService.sendPreview(to, req.getSubject(), req.getContent());
        
        // Lưu lịch sử email preview
        EmailHistory history = new EmailHistory(
            req.getSubject(), 
            req.getContent(), 
            "preview", 
            "sent", 
            "admin" // TODO: lấy từ authentication
        );
        history.setRecipientEmail(to);
        history.setTotalSent(1);
        history.setSentAt(LocalDateTime.now());
        emailHistoryService.saveEmailHistory(history);
        
        return ResponseEntity.ok("Preview sent");
    }
    
    // API lấy lịch sử email
    @GetMapping("/history")
    public ResponseEntity<?> getEmailHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "emailType", required = false) String emailType,
            @RequestParam(name = "status", required = false) String status) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<EmailHistory> history;
        
        if (emailType != null) {
            history = emailHistoryService.getEmailHistoryByType(emailType, pageable);
        } else {
            history = emailHistoryService.getEmailHistory(pageable);
        }
        
        return ResponseEntity.ok(history);
    }
    
    // API lấy thống kê email
    @GetMapping("/statistics")
    public ResponseEntity<?> getEmailStatistics() {
        Map<String, Object> stats = emailHistoryService.getEmailStatistics();
        return ResponseEntity.ok(stats);
    }
    
    // API lấy chi tiết email
    @GetMapping("/history/{id}")
    public ResponseEntity<?> getEmailHistoryById(@PathVariable(name = "id") Long id) {
        EmailHistory history = emailHistoryService.getEmailHistoryById(id);
        if (history == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(history);
    }
    
    // API test scheduler (gửi email đã lên lịch ngay lập tức)
    @PostMapping("/test-scheduler")
    public ResponseEntity<?> testScheduler() {
        try {
            emailSchedulerService.processScheduledEmails();
            return ResponseEntity.ok(java.util.Map.of("message", "Scheduler test completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}


