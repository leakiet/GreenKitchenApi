package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.ScheduledEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledEmailRepository extends JpaRepository<ScheduledEmail, Long> {
    List<ScheduledEmail> findBySentFlagFalseAndSendAtLessThanEqual(LocalDateTime sendAt);
}


