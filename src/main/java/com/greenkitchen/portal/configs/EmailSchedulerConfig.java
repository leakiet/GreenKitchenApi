package com.greenkitchen.portal.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

@Configuration
public class EmailSchedulerConfig implements SchedulingConfigurer {

    @Value("${app.email.scheduler.pool-size:5}")
    private int poolSize;

    @Value("${app.email.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (schedulerEnabled) {
            // Sử dụng thread pool riêng cho email scheduling
            taskRegistrar.setScheduler(Executors.newScheduledThreadPool(poolSize));
        }
    }
}
