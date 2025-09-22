package com.greenkitchen.portal.services;

import com.greenkitchen.portal.entities.ScheduledHolidayEmail;
import com.greenkitchen.portal.dtos.HolidayEmailScheduleRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledHolidayEmailService {
    
    /**
     * Create a new scheduled holiday email
     */
    ScheduledHolidayEmail createScheduledEmail(HolidayEmailScheduleRequest request, String holidayName, LocalDateTime holidayDate);
    
    /**
     * Get all scheduled holiday emails
     */
    List<ScheduledHolidayEmail> getAllScheduledEmails();
    
    /**
     * Get scheduled emails by holiday ID
     */
    List<ScheduledHolidayEmail> getScheduledEmailsByHolidayId(Long holidayId);
    
    /**
     * Get scheduled emails for a specific year
     */
    List<ScheduledHolidayEmail> getScheduledEmailsByYear(int year);
    
    /**
     * Get scheduled email by ID
     */
    ScheduledHolidayEmail getScheduledEmailById(Long id);
    
    /**
     * Update scheduled email
     */
    ScheduledHolidayEmail updateScheduledEmail(Long id, HolidayEmailScheduleRequest request);
    
    /**
     * Delete scheduled email
     */
    void deleteScheduledEmail(Long id);
    
    /**
     * Get pending emails that should be sent now
     */
    List<ScheduledHolidayEmail> getPendingEmailsToSend();
    
    /**
     * Mark email as sent
     */
    ScheduledHolidayEmail markAsSent(Long id, int sentCount);
    
    /**
     * Mark email as failed
     */
    ScheduledHolidayEmail markAsFailed(Long id, String errorMessage);
    
    /**
     * Cancel scheduled email
     */
    ScheduledHolidayEmail cancelScheduledEmail(Long id);
    
    /**
     * Get statistics
     */
    ScheduledHolidayEmailStats getStatistics();
    
    class ScheduledHolidayEmailStats {
        public long totalScheduled;
        public long pendingCount;
        public long sentCount;
        public long failedCount;
        public long cancelledCount;
    }
}
