package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.entities.ScheduledEmail;
import com.greenkitchen.portal.repositories.ScheduledEmailRepository;
import com.greenkitchen.portal.services.ScheduledEmailService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class ScheduledEmailServiceImpl implements ScheduledEmailService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledEmailServiceImpl.class);

    @Autowired
    private ScheduledEmailRepository scheduledEmailRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ThreadPoolTaskScheduler emailTaskScheduler;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        LocalDateTime now = LocalDateTime.now();
        scheduledEmailRepository.findAll().stream()
            .filter(e -> !e.isSentFlag())
            .forEach(e -> {
                if (e.getSendAt() == null) return;
                if (!e.getSendAt().isAfter(now)) {
                    log.info("Triggering overdue scheduled email id={} immediately", e.getId());
                    triggerSend(e);
                } else {
                    scheduleExisting(e);
                }
            });
    }

    @Override
    public ScheduledEmail scheduleEmail(ScheduledEmail email) {
        email.setSentFlag(false);
        ScheduledEmail saved = scheduledEmailRepository.save(email);
        if (!saved.getSendAt().isAfter(LocalDateTime.now())) {
            log.info("Triggering immediate send for scheduled email id={}", saved.getId());
            triggerSend(saved);
        } else {
            scheduleExisting(saved);
        }
        return saved;
    }

    private void scheduleExisting(ScheduledEmail email) {
        java.time.Instant triggerTime = email.getSendAt().atZone(ZoneId.systemDefault()).toInstant();
        Runnable task = () -> {
            log.info("Scheduled email trigger fired id={} at {}", email.getId(), LocalDateTime.now());
            triggerSend(email);
        };
        ScheduledFuture<?> future = emailTaskScheduler.schedule(task, triggerTime);
        scheduledTasks.put(email.getId(), future);
        log.info("Scheduled email id={} at {}", email.getId(), email.getSendAt());
    }

    private void triggerSend(ScheduledEmail email) {
        try {
            if (email.isSentFlag()) return;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email.getRecipient());
            message.setSubject(email.getSubject());
            message.setText(email.getContent());
            mailSender.send(message);

            email.setSentFlag(true);
            scheduledEmailRepository.save(email);
            log.info("Email sent and marked sentFlag=true id={}", email.getId());
        } catch (Exception ex) {
            log.error("Failed to send scheduled email id={}: {}", email.getId(), ex.getMessage());
        } finally {
            ScheduledFuture<?> f = scheduledTasks.remove(email.getId());
            if (f != null) {
                f.cancel(false);
            }
        }
    }
}


