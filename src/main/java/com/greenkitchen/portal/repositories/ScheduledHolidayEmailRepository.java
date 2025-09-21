package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.ScheduledHolidayEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledHolidayEmailRepository extends JpaRepository<ScheduledHolidayEmail, Long> {
    
    // Find all scheduled emails
    List<ScheduledHolidayEmail> findAllByOrderByScheduleAtAsc();
    
    // Find scheduled emails by holiday ID
    List<ScheduledHolidayEmail> findByHolidayIdOrderByScheduleAtAsc(Long holidayId);
    
    // Find scheduled emails by status
    List<ScheduledHolidayEmail> findByStatusOrderByScheduleAtAsc(ScheduledHolidayEmail.EmailStatus status);
    
    // Find pending emails that should be sent now
    @Query("SELECT s FROM ScheduledHolidayEmail s WHERE s.status = 'PENDING' AND s.scheduleAt <= :now AND s.isActive = true")
    List<ScheduledHolidayEmail> findPendingEmailsToSend(@Param("now") LocalDateTime now);
    
    // Find scheduled emails for a specific year
    @Query("SELECT s FROM ScheduledHolidayEmail s WHERE YEAR(s.holidayDate) = :year ORDER BY s.scheduleAt ASC")
    List<ScheduledHolidayEmail> findByHolidayYear(@Param("year") int year);
    
    // Count scheduled emails by status
    long countByStatus(ScheduledHolidayEmail.EmailStatus status);
    
    // Find scheduled emails by target audience
    List<ScheduledHolidayEmail> findByTargetAudienceOrderByScheduleAtAsc(String targetAudience);
    
    // Check if holiday already has scheduled email
    boolean existsByHolidayIdAndStatus(Long holidayId, ScheduledHolidayEmail.EmailStatus status);
}
