package com.greenkitchen.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class EmailSchedulingConfig {

    @Bean
    public ThreadPoolTaskScheduler emailTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("email-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}


