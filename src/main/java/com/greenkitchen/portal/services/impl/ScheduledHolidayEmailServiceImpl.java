package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.dtos.HolidayEmailScheduleRequest;
import com.greenkitchen.portal.entities.ScheduledHolidayEmail;
import com.greenkitchen.portal.repositories.ScheduledHolidayEmailRepository;
import com.greenkitchen.portal.services.ScheduledHolidayEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ScheduledHolidayEmailServiceImpl implements ScheduledHolidayEmailService {
    
    @Autowired
    private ScheduledHolidayEmailRepository repository;
    
    @Override
    public ScheduledHolidayEmail createScheduledEmail(HolidayEmailScheduleRequest request, String holidayName, LocalDateTime holidayDate) {
        ScheduledHolidayEmail scheduledEmail = new ScheduledHolidayEmail();
        scheduledEmail.setHolidayId(request.getHolidayId());
        scheduledEmail.setHolidayName(holidayName);
        scheduledEmail.setHolidayDate(holidayDate);
        scheduledEmail.setSubject(request.getCustomSubject());
        scheduledEmail.setContent(request.getCustomContent());
        scheduledEmail.setScheduleAt(request.getScheduleAt());
        scheduledEmail.setTargetAudience(request.getTargetAudience());
        scheduledEmail.setIsActive(request.isActive());
        scheduledEmail.setDaysBefore(request.getDaysBefore());
        scheduledEmail.setTemplateType(request.getTemplateType());
        scheduledEmail.setStatus(ScheduledHolidayEmail.EmailStatus.PENDING);
        
        return repository.save(scheduledEmail);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScheduledHolidayEmail> getAllScheduledEmails() {
        return repository.findAllByOrderByScheduleAtAsc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScheduledHolidayEmail> getScheduledEmailsByHolidayId(Long holidayId) {
        return repository.findByHolidayIdOrderByScheduleAtAsc(holidayId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScheduledHolidayEmail> getScheduledEmailsByYear(int year) {
        return repository.findByHolidayYear(year);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ScheduledHolidayEmail getScheduledEmailById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled email not found with id: " + id));
    }
    
    @Override
    public ScheduledHolidayEmail updateScheduledEmail(Long id, HolidayEmailScheduleRequest request) {
        ScheduledHolidayEmail scheduledEmail = getScheduledEmailById(id);
        
        scheduledEmail.setSubject(request.getCustomSubject());
        scheduledEmail.setContent(request.getCustomContent());
        scheduledEmail.setScheduleAt(request.getScheduleAt());
        scheduledEmail.setTargetAudience(request.getTargetAudience());
        scheduledEmail.setIsActive(request.isActive());
        scheduledEmail.setDaysBefore(request.getDaysBefore());
        scheduledEmail.setTemplateType(request.getTemplateType());
        
        return repository.save(scheduledEmail);
    }
    
    @Override
    public void deleteScheduledEmail(Long id) {
        ScheduledHolidayEmail scheduledEmail = getScheduledEmailById(id);
        repository.delete(scheduledEmail);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScheduledHolidayEmail> getPendingEmailsToSend() {
        return repository.findPendingEmailsToSend(LocalDateTime.now());
    }
    
    @Override
    public ScheduledHolidayEmail markAsSent(Long id, int sentCount) {
        ScheduledHolidayEmail scheduledEmail = getScheduledEmailById(id);
        scheduledEmail.setStatus(ScheduledHolidayEmail.EmailStatus.SENT);
        scheduledEmail.setSentAt(LocalDateTime.now());
        scheduledEmail.setSentCount(sentCount);
        
        return repository.save(scheduledEmail);
    }
    
    @Override
    public ScheduledHolidayEmail markAsFailed(Long id, String errorMessage) {
        ScheduledHolidayEmail scheduledEmail = getScheduledEmailById(id);
        scheduledEmail.setStatus(ScheduledHolidayEmail.EmailStatus.FAILED);
        // Could add error message field if needed
        
        return repository.save(scheduledEmail);
    }
    
    @Override
    public ScheduledHolidayEmail cancelScheduledEmail(Long id) {
        ScheduledHolidayEmail scheduledEmail = getScheduledEmailById(id);
        scheduledEmail.setStatus(ScheduledHolidayEmail.EmailStatus.CANCELLED);
        scheduledEmail.setIsActive(false);
        
        return repository.save(scheduledEmail);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ScheduledHolidayEmailStats getStatistics() {
        ScheduledHolidayEmailStats stats = new ScheduledHolidayEmailStats();
        stats.totalScheduled = repository.count();
        stats.pendingCount = repository.countByStatus(ScheduledHolidayEmail.EmailStatus.PENDING);
        stats.sentCount = repository.countByStatus(ScheduledHolidayEmail.EmailStatus.SENT);
        stats.failedCount = repository.countByStatus(ScheduledHolidayEmail.EmailStatus.FAILED);
        stats.cancelledCount = repository.countByStatus(ScheduledHolidayEmail.EmailStatus.CANCELLED);
        
        return stats;
    }
}
