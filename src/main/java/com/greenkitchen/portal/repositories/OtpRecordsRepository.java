package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.OtpRecords;
import java.time.LocalDateTime;

@Repository
public interface OtpRecordsRepository extends JpaRepository<OtpRecords, Long> {
    OtpRecords findByEmailAndOtpCode(String email, String otpCode);
    
    // Find recent OTP record within specified time for rate limiting
    @Query("SELECT o FROM OtpRecords o WHERE o.email = :email AND o.createdAt > :timeThreshold ORDER BY o.createdAt DESC")
    OtpRecords findRecentOtpByEmailAndTime(@Param("email") String email, @Param("timeThreshold") LocalDateTime timeThreshold);
    
    // Mark all previous OTPs as used for an email
    @Modifying
    @Transactional
    @Query("UPDATE OtpRecords o SET o.isUsed = true WHERE o.email = :email AND o.isUsed = false")
    void markAllOtpAsUsedByEmail(@Param("email") String email);
}
